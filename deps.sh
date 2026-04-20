#!/bin/sh
set -e

MVN=https://repo1.maven.org/maven2
DEST=build/deps
mkdir -p "$DEST"

# group-path                     artifact                 version
DEPS="
com/fasterxml/jackson/core       jackson-core             2.12.5
com/fasterxml/jackson/core       jackson-databind         2.12.5
com/fasterxml/jackson/core       jackson-annotations      2.12.5
com/fasterxml/jackson/dataformat jackson-dataformat-yaml  2.12.5
org/yaml                         snakeyaml                1.27
org/junit/jupiter                junit-jupiter-api        5.7.0
org/junit/jupiter                junit-jupiter-params     5.7.0
org/junit/jupiter                junit-jupiter-engine     5.7.0
org/junit/platform               junit-platform-engine    1.7.0
org/junit/platform               junit-platform-commons   1.7.0
org/junit/platform               junit-platform-launcher  1.7.0
org/junit/platform               junit-platform-console   1.7.0
org/junit/platform               junit-platform-reporting 1.7.0
org/opentest4j                   opentest4j               1.2.0
org/apiguardian                  apiguardian-api          1.1.0
"

echo "$DEPS" | while read -r group artifact version; do
    [ -z "$artifact" ] && continue
    jar="$artifact-$version.jar"
    [ -f "$DEST/$jar" ] && continue
    echo "  Downloading $jar"
    curl -fsSL -o "$DEST/$jar" "$MVN/$group/$artifact/$version/$jar"
done
