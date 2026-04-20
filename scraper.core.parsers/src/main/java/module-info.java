import scraper.api.Command;
import scraper.api.ScrapeSpecificationParser;
import scraper.plugins.core.parser.*;

open module scraper.core.parsers {
    exports scraper.plugins.core.parser;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;

    requires transitive scraper.api;
    requires transitive scraper.core;

    provides ScrapeSpecificationParser with JsonParser, YamlParser, SimpleYamlParser;
    provides Command with JsonParser, YamlParser, SimpleYamlParser;
}
