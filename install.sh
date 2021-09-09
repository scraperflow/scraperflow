#!/bin/bash

./gradlew distZip
unzip -o build/distributions/*.zip -d build/distributions/

rm -rf ~/opt/scraperflow/
mkdir -p ~/opt/scraperflow/
cp -r build/distributions/scraperflow*/* ~/opt/scraperflow/
