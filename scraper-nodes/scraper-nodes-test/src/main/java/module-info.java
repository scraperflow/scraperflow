import scraper.api.node.Node;
import scraper.nodes.test.AssertNode;

open module scraper.nodes.test {
    requires scraper.annotations;
    requires scraper.api;
    requires scraper.core;
    requires org.eclipse.jetty.server;
    requires org.slf4j;

    requires JUnitParams;
    requires junit;

    exports scraper.nodes.test;
    exports scraper.nodes.test.annotations;
    exports scraper.nodes.test.helper;

    // FIXME why is this needed so that reflections can find all nodes?
    provides Node with AssertNode;
}
