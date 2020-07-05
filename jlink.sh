#!/bin/bash
jlink --no-header-files --no-man-pages --compress=2 --strip-debug --module-path application/build/distributions/application-1.0.2/lib --add-modules scraper.app --output jre --launcher launch=scraper.app/scraper.app.Scraper
