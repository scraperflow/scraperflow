#!/bin/bash

./gradlew distZip
unzip -o build/distributions/*.zip -d build/distributions/

rm -rf ~/.local/bin/scraperflow/
mkdir -p ~/.local/bin/scraperflow/
cp -r build/distributions/scraperflow*/* ~/.local/bin/scraperflow/
