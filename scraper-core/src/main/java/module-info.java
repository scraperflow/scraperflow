import scraper.api.node.type.Node;
import scraper.core.AbstractMetadata;
import scraper.api.node.container.NodeContainer;

open module scraper.core {
    uses AbstractMetadata;
    uses scraper.api.plugin.Addon;
    uses NodeContainer;
    uses Node;

    exports scraper.core;
    exports scraper.core.template;
    exports scraper.util;
    exports scraper.api.flow.impl;
    exports scraper.api.specification.impl;

    requires scraper.annotations;
    requires scraper.api;
    requires scraper.utils;


    requires io.github.classgraph;
    requires com.google.common;
    requires java.net.http;

    requires org.slf4j;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;

    requires org.antlr.antlr4.runtime;
}