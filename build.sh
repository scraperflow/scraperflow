#!/bin/sh
set -e

javac -d build/classes --module-path build/deps \
  --module-source-path "./*/src/main/java" \
  --module scraper.api,scraper.utils,scraper.core,scraper.core.parsers,scraper.core.plugins,scraper.test,scraper.app
