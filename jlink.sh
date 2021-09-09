#!/bin/bash
set -e

# build
rm -rf build/jre
./gradlew distZip
unzip -o build/distributions/*.zip -d build/distributions/


DIST=`uname -m`
rm -rf build/$DIST
mkdir -p build/$DIST

#jlink --add-modules ALL-MODULE-PATH --no-header-files --no-man-pages --compress=2 --strip-debug --module-path build/distributions/scraperflow-1.0.0-rc1/lib --add-modules scraper.app --output build/jre
jlink --add-modules ALL-MODULE-PATH --module-path build/distributions/scraperflow-1.0.0-rc1/lib --add-modules scraper.app --output build/jre
cp dist/scraperflow build/$DIST/
cp -r build/jre build/$DIST/
sed -i s/java/\$SDIR\\/jre\\/bin\\/java/ build/$DIST/scraperflow
