import scraper.api.node.Node;
import scraper.nodes.core.functional.EchoNode;

open module scraper.nodes.core {
    requires scraper.annotations;
    requires scraper.api;
    requires scraper.core;

    exports scraper.nodes.core.functional;
    exports scraper.nodes.core.flow;
    exports scraper.nodes.core.io;

    // FIXME why is this needed so that reflections can find all nodes?
    provides Node with EchoNode;
}
