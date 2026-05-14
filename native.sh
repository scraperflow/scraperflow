#!/bin/sh
set -e

# Builds a single-file pure native-image of scraperflow.
#
# Usage: native.sh [--include <dir>]...
#
#   --include DIR   Recursively scan DIR for module-info.java descriptors and
#                   include each discovered module in the image. Repeatable.
#
# Output: build/native/scraperflow  (one ELF, no siblings).
#
# Sits entirely on --module-path. For the java.net.http requires-chain we
# pass --add-modules=java.net.http explicitly (otherwise native-image's
# layer construction tries to redefine the boot-layer module).

command -v native-image >/dev/null 2>&1 || {
    echo "native-image not on PATH. Install GraalVM 21+ and try again." >&2
    exit 1
}

INCLUDES=
while [ $# -gt 0 ]; do
    case "$1" in
        --include) INCLUDES="$INCLUDES $2"; shift 2;;
        *) echo "unknown option: $1" >&2; exit 2;;
    esac
done

CORE_MODULES="scraper.api scraper.utils scraper.core scraper.core.parsers scraper.core.plugins scraper.app"

OUT=build/native
rm -rf "$OUT"
mkdir -p "$OUT"

# ---- 1. Discover --include module sources -------------------------------
EXTRA_MODULES=
rm -rf build/extra-src build/extra-classes
mkdir -p build/extra-src build/extra-classes
for dir in $INCLUDES; do
    [ -d "$dir" ] || { echo "--include: no such directory: $dir" >&2; exit 2; }
    for mi in $(find "$dir" -name module-info.java -type f); do
        moddir=$(dirname "$mi")
        modname=$(grep -oE 'module +[A-Za-z0-9_.]+' "$mi" | awk '{print $2}' | head -1)
        [ -n "$modname" ] || { echo "could not parse module name from $mi" >&2; exit 1; }
        mkdir -p "build/extra-src/$modname"
        cp -r "$moddir/." "build/extra-src/$modname/"
        EXTRA_MODULES="$EXTRA_MODULES $modname"
        echo "--include: $modname ($moddir)"
    done
done

# ---- 2. Compile extra modules -------------------------------------------
if [ -n "$EXTRA_MODULES" ]; then
    javac -d build/extra-classes \
        --module-path build/classes:build/deps \
        --module-source-path 'build/extra-src/*' \
        --module "$(echo $EXTRA_MODULES | tr ' ' ',')"
fi

# ---- 3. Production-only module staging (exclude scraper.test) -----------
PROD_CLASSES=build/prod-classes
rm -rf "$PROD_CLASSES"
mkdir -p "$PROD_CLASSES"
for m in $CORE_MODULES; do
    cp -r "build/classes/$m" "$PROD_CLASSES/"
done
for m in $EXTRA_MODULES; do
    cp -r "build/extra-classes/$m" "$PROD_CLASSES/"
done

# ---- 4. Blanket reflect-config.json -------------------------------------
mkdir -p build/tools
if [ ! -f build/tools/ExtractServices.class ] || [ build/ExtractServices.java -nt build/tools/ExtractServices.class ]; then
    javac -d build/tools build/ExtractServices.java
fi
java -cp build/tools ExtractServices "$PROD_CLASSES"

# ---- 5. Pack each module as a jar ---------------------------------------
GUEST=build/guest-jars
rm -rf "$GUEST"
mkdir -p "$GUEST"
for m in $CORE_MODULES $EXTRA_MODULES; do
    jar --create --file "$GUEST/$m.jar" -C "$PROD_CLASSES/$m" .
done

# ---- 6. Runtime-only deps (Jackson + SnakeYAML) -------------------------
# JUnit / apiguardian / opentest4j are test-only; exclude.
RUNTIME_DEPS=build/runtime-deps
rm -rf "$RUNTIME_DEPS"
mkdir -p "$RUNTIME_DEPS"
for j in build/deps/jackson-core-*.jar \
         build/deps/jackson-databind-*.jar \
         build/deps/jackson-annotations-*.jar \
         build/deps/jackson-dataformat-yaml-*.jar \
         build/deps/snakeyaml-*.jar; do
    cp "$j" "$RUNTIME_DEPS/"
done

MP="$GUEST:$RUNTIME_DEPS"

# ---- 6b. build-libs.tar (embedded classpath for scraperflow build-node) --
# scraperflow build-node compiles helper node source files using javac +
# native-image, and both need scraper.* + Jackson on classpath. Stage those
# as jars in a tar and embed it as a scraper.app resource so the subcommand
# can extract it on demand.
BUILD_LIB_STAGE=$(mktemp -d)
trap 'rm -rf "$BUILD_LIB_STAGE"' EXIT
for m in scraper.api scraper.utils scraper.core scraper.test; do
    jar --create --file "$BUILD_LIB_STAGE/$m.jar" -C "build/classes/$m" .
done
for j in build/deps/jackson-core-*.jar \
         build/deps/jackson-databind-*.jar \
         build/deps/jackson-annotations-*.jar; do
    cp "$j" "$BUILD_LIB_STAGE/"
done
mkdir -p "$PROD_CLASSES/scraper.app/scraper/app"
tar cf "$PROD_CLASSES/scraper.app/scraper/app/build-libs.tar" \
    -C "$BUILD_LIB_STAGE" .
# Re-jar scraper.app so the tar is inside its module jar.
jar --create --file "$GUEST/scraper.app.jar" -C "$PROD_CLASSES/scraper.app" .

# ---- 7. Native-image build on --module-path -----------------------------
native-image \
    --module-path "$MP" \
    --module scraper.app/scraper.app.Scraper \
    --add-modules=java.net.http \
    -H:Name=scraperflow \
    -H:Path="$OUT" \
    -H:ReflectionConfigurationFiles="$PROD_CLASSES/reflect-config.json" \
    -H:IncludeResources='scraper/app/build-libs\.tar$' \
    --no-fallback \
    --enable-url-protocols=http,https,file \
    --initialize-at-build-time=com.fasterxml.jackson,org.yaml.snakeyaml

echo
ls -lh "$OUT/scraperflow"
