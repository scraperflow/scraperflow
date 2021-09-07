ScraperFlow - A Composable Workflow Framework
=========================================

[![](https://jitpack.io/v/scraperflow/scraperflow.svg)](https://jitpack.io/#scraperflow/scraperflow)
![language](https://img.shields.io/badge/language-java11+(JPMS)-blue.svg)
![build](https://img.shields.io/badge/build-gradle-yellowgreen.svg)

![Java CI with Gradle](https://github.com/scraperflow/scraperflow/workflows/Java%20CI%20with%20Gradle/badge.svg)

ScraperFlow is a framework which enables flow-based programming in a declarative way. 
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

* [ScraperFlow Node Documentation](https://docs.scraperflow.server1.link)
  * The documentation contains **all** nodes, including [extra nodes](https://github.com/scraperflow/scraperflow-nodes), not only the nodes in the core framework
* [ScraperFlow Wiki](https://wiki.scraperflow.server1.link)
* [ScraperFlow Binaries](https://binaries.scraperflow.server1.link)
* [ScraperFlow Editor (prototype, deprecated)](https://editor.scraperflow.server1.link)

# Documentation

The documentation can be found at the [ScraperFlow Wiki](https://wiki.scraperflow.server1.link).

# Quickstart - Specification

A minimal specification that can be used for any of the quickstart sections:

```yml
start:
 - {f: echo, log: hello world}
```

# Quickstart - Docker

ScraperFlow is deployed to [Dockerhub](https://hub.docker.com/repository/docker/albsch/scraperflow).

To use a ScraperFlow container once, use

    docker run -v "$PWD":/rt/ -v "$PWD":/nodes -v "$PWD":/plugins --rm albsch/scraperflow:latest help

and place your workflow in the current workflow directory. 
'$PWD' can be changed to another working directory if needed.
If custom nodes or plugins are to be supplied (like [dev-nodes](https://github.com/scraperflow/scraperflow-nodes/releases)),
place the jar(s) in the current working directory (or change '$PWD'), too.


# Quickstart - Java

ScraperFlow is fully modularized.

Get the latest [modular jar bundle](https://github.com/scraperflow/scraperflow/releases) 
and any [plugin jar](https://github.com/scraperflow/scraperflow-plugins) 
or [additional node jars](https://github.com/scraperflow/scraperflow-nodes) you like.

Place the additional plugins and nodes in a `var` folder where the run script
resides.
Use the provided run script to run ScraperFlow.
       
ScraperFlow will look for workflows relative to the working directory.


# Quickstart - Java Native

Execute `./gradlew installDist`. This will install scraper in your home
directory at `~/opt/scraperflow`. 
A scraperflow start script can then be executed via
`~/opt/scraperflow/scraperflow`.
Additional plugin jars can be put into `~/opt/scraperflow/var`.


# Quickstart - Development

Using

      gradle clean build codeCov

will

* compile the project 
* test the project
* package the project at `application/build/distributions`
* generate code coverage report at `build/reports/jacoco/codeCoverageReport/html/index.html`

Specification parsers are plugins and need to be provided on the module path.
Executing scraper in a IDE requires the module path to be extended with the following JVM parameter:

    --add-modules ALL-MODULE-PATH
