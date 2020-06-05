import scraper.api.plugin.Hook;
import scraper.api.plugin.ScrapeSpecificationParser;
import scraper.plugins.core.flowgraph.ControlFlowGraphGeneratorHook;
import scraper.plugins.core.parser.JsonParser;

open module scraper.plugins.core {

    requires scraper.annotations;
    requires scraper.api;
    requires scraper.core;
    requires scraper.utils;

    requires org.slf4j;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;

//    // FIXME why is this needed so that reflections can find all nodes?
    provides Hook with ControlFlowGraphGeneratorHook;
    provides ScrapeSpecificationParser with JsonParser;
}
