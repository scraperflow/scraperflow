import scraper.api.plugin.Hook;
import scraper.plugins.core.flowgraph.ControlFlowGraphGeneratorHook;

open module scraper.plugins.core {
    requires scraper.annotations;
    requires scraper.api;
    requires scraper.core;
    requires scraper.utils;


    requires org.slf4j;

    requires jdk.javadoc;

//    // FIXME why is this needed so that reflections can find all nodes?
    provides Hook with ControlFlowGraphGeneratorHook;
}
