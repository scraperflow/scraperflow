#!/bin/bash

java Build.java dist

rm -rf ~/.local/bin/scraperflow/
mkdir -p ~/.local/bin/scraperflow/
cp -r build/dist/scraperflow/* ~/.local/bin/scraperflow/
