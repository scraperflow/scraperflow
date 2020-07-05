import scraper.api.plugin.ScrapeSpecificationParser;
import scraper.plugins.core.parser.JsonParser;
import scraper.plugins.core.parser.YamlParser;

open module scraper.core.parsers {
    exports scraper.plugins.core.parser;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;

    requires transitive scraper.api;
    requires transitive scraper.core;

    provides ScrapeSpecificationParser with JsonParser, YamlParser;
}
