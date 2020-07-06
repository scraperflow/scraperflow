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

    requires transitive io.github.classgraph;
    requires java.net.http;

    requires transitive com.fasterxml.jackson.databind;

    requires transitive scraper.api;
    requires transitive scraper.utils;

    // TODO
    requires org.antlr.antlr4.runtime;
}