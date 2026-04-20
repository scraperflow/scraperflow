#!/bin/sh
set -e

MODS="scraper.api scraper.utils scraper.core scraper.core.parsers scraper.core.plugins scraper.test"
AUX=scraper.core.testproviders
ALL=$(echo $MODS $AUX | tr ' ' ,),org.junit.jupiter.api,org.junit.jupiter.params,org.junit.jupiter.engine
MP=build/deps:build/classes:build/test-aux

for m in $MODS; do
    S=$m/src/test/java
    [ -d "$S" ] || continue
    D=build/test-classes/$m
    mkdir -p $D

    javac -d $D -p $MP --patch-module $m=$S \
        --add-modules $ALL --add-reads $m=$ALL \
        $(find $S -name '*.java' ! -name module-info.java)

    [ -d "$m/src/test/resources" ] && cp -r $m/src/test/resources/. $D/

    # JUnit needs each test package opened for reflection.
    OPENS=""
    for p in $(find $S -name '*.java' -exec dirname {} \; | sort -u | sed "s|^$S/||;s|/|.|g"); do
        OPENS="$OPENS --add-opens $m/$p=org.junit.platform.commons"
    done

    echo "== Testing $m =="
    java -p $MP:$D --add-modules ALL-MODULE-PATH \
        --patch-module $m=$D --add-reads $m=$ALL $OPENS \
        -m org.junit.platform.console/org.junit.platform.console.ConsoleLauncher \
        --select-module $m --fail-if-no-tests
done
