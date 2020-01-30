import scraper.api.node.type.Node;
import scraper.nodes.core.functional.EchoNode;

open module scraper.nodes.core {
    requires scraper.annotations;
    requires scraper.api;
    requires scraper.core;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires scraper.utils;

    // FIXME why is this needed so that reflections can find all nodes?
    provides Node with EchoNode;
}
