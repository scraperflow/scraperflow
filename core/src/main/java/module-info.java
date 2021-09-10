import scraper.api.*;
import scraper.core.AbstractMetadata;
import scraper.core.JobFactory;

open module scraper.core {
    uses AbstractMetadata;
    uses Addon;
    uses NodeContainer;
    uses Node;
    uses ScrapeSpecificationParser;
    uses NodeHook;
    uses Hook;

    exports scraper.core;
    exports scraper.core.template;
    exports scraper.util;
    exports scraper.api.flow.impl;
    exports scraper.api.specification.impl;

    requires java.net.http;

    requires transitive com.fasterxml.jackson.databind;

    requires transitive scraper.api;
    requires transitive scraper.utils;
    requires java.compiler;

    provides Command with JobFactory.JobFactoryCommand;
}