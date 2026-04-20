#!/bin/sh
set -e

# Builds a self-contained, architecture-specific bundle with a custom JRE.
# Assumes the pipeline has run: deps.sh, build.sh.

DIST=$(uname -m)
BUNDLE=build/$DIST
rm -rf "$BUNDLE" build/jre

# 1. Custom JRE with scraper.app's transitive closure (JDK + our core modules
#    + jackson core/databind/annotations).
#    scraper.core.parsers is NOT in the closure: it requires jackson-dataformat-yaml,
#    which requires the automatic module snakeyaml — jlink refuses automatic modules.
#    The parsers layer therefore ships in lib/ and is loaded at runtime.
jlink --module-path build/classes:build/deps \
      --add-modules scraper.app \
      --output build/jre

# 2. Runtime lib/ with everything not baked into the JRE.
mkdir -p "$BUNDLE/lib"
for m in scraper.core.parsers scraper.core.plugins scraper.test; do
    jar --create --file "$BUNDLE/lib/$m.jar" -C "build/classes/$m" .
done
cp build/deps/jackson-dataformat-yaml-*.jar build/deps/snakeyaml-*.jar "$BUNDLE/lib/"

# 3. Bundle the JRE and the launch script; rewrite it to use the bundled java.
cp -r build/jre "$BUNDLE/"
cp dist/scraperflow "$BUNDLE/"
sed -i 's|^java |$SDIR/jre/bin/java |' "$BUNDLE/scraperflow"

echo "Bundle created at: $BUNDLE"
