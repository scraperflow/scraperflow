#!/bin/sh
set -e

# Compile test-scoped auxiliary modules into build/test-aux/.
# Production modules (in build/classes/) are referenced via the module path
# so their sources aren't recompiled.
AUX=scraper.core.testproviders
javac -d build/test-aux --module-path build/deps:build/classes \
  --module-source-path "$AUX=$AUX/src/main/java" \
  --module "$AUX"
