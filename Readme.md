Scraper - A Composable Workflow Framework
=========================================

![version](https://img.shields.io/badge/version-0.14.1-green.svg)
![language](https://img.shields.io/badge/language-java9+(JPMS)-blue.svg)
![build](https://img.shields.io/badge/build-gradle-yellowgreen.svg)

[![pipeline status](https://git.server1.link/scraper/scraper/badges/master/pipeline.svg)](https://git.server1.link/scraper/scraper/commits/master)
[![coverage report](https://git.server1.link/scraper/scraper/badges/master/coverage.svg)](https://git.server1.link/scraper/scraper/commits/master)

Scraper is a framework which enables flow-based programming in a declarative way. 
It is based on two main components: 
the core which translates the declarative description (JSON or YAML) into a format that is understood by
the framework, and the actual nodes which can be used to construct a workflow.
The architecture is plugin-based, so nodes can be implemented on their own and provided
to the framework.

The main goal of this framework is to facilitate reuse of code (nodes) and help
managing control flow of programs in an easy way (declarative workflow specification).

The workflow specification is statically checked to ensure that the configuration is well-typed 
against the composition of nodes.

# Links

* [Scraper Node Documentation](https://docs.scraper.server1.link)
* [Scraper Wiki](https://wiki.scraper.server1.link)
* [Scraper Binaries](https://binaries.scraper.server1.link)
* [Scraper Editor (prototype, deprecated)](https://editor.scraper.server1.link)

# Documentation

The documentation can be found at the [Scraper Wiki](https://wiki.scraper.server1.link).

# Quickstart - Docker

Scraper is deployed to [Dockerhub](https://hub.docker.com/repository/docker/albsch/scraper).

To use a Scraper container once, use

    docker run -v "$PWD":/rt/ -v "$PWD":/nodes -v "$PWD":/plugins --rm albsch/scraper:latest help

and place your workflow in the current workflow directory. 
'$PWD' can be changed to another working directory if needed.
If custom nodes or plugins are to be supplied (like [dev-nodes](https://github.com/scraperflow/scraper-nodes/releases)),
place the jar(s) in the current working directory (or change '$PWD'), too.


# Quickstart - Java

Scraper is fully modularized.

Get the latest [jar release bundle](https://github.com/scraperflow/scraper/releases) 
and any [plugin](https://github.com/scraperflow/scraper-plugins) 
or [additional nodes](https://github.com/scraperflow/scraper-nodes) you like.

Place them in the same folder. 
To run Scraper, use

         java -p . -m scraper.app help
       
Scraper will look for workflows relative to the working directory.

# Quickstart - Development

Using

      gradle clean test build codeCov

will

* compile the project 
* test the project
* package the project at `application/build/distributions`
* generate code coverage report at `build/reports/jacoco/codeCoverageReport/html/index.html`

