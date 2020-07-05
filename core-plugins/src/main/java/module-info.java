import scraper.api.node.type.Node;
import scraper.api.plugin.Hook;
import scraper.hooks.ExitHook;
import scraper.hooks.NodeDependencyGeneratorHook;
import scraper.nodes.core.functional.EchoNode;

open module scraper.core.plugins {
    exports scraper.hooks;
    exports scraper.nodes.core.example;
    exports scraper.nodes.core.flow;
    exports scraper.nodes.core.functional;
    exports scraper.nodes.core.io;
    exports scraper.nodes.core.os;
    exports scraper.nodes.core.stream;
    exports scraper.nodes.core.time;


    requires transitive scraper.core;

    provides Hook with ExitHook, NodeDependencyGeneratorHook;
    provides Node with EchoNode;
}
