import scraper.api.Addon;
import scraper.api.Node;
import scraper.core.AbstractMetadata;
import scraper.api.NodeContainer;

open module scraper.core {
    uses AbstractMetadata;
    uses Addon;
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
    requires java.compiler;

}