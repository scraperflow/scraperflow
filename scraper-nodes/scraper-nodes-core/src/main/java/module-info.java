import scraper.api.node.Node;
import scraper.nodes.core.functional.EchoNode;

open module scraper.nodes.core {
    requires scraper.annotations;
    requires scraper.api;
    requires scraper.core;

    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires scraper.utils;

    exports scraper.nodes.core.functional;
    exports scraper.nodes.core.flow;
    exports scraper.nodes.core.io;
    exports scraper.nodes.core.stream;
    exports scraper.nodes.core.time;

    // FIXME why is this needed so that reflections can find all nodes?
    provides Node with EchoNode;
}
