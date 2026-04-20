import scraper.api.*;
import scraper.core.JobFactory;

module scraper.app {
    uses Addon;
    uses Hook;
    uses NodeHook;
    uses ScrapeSpecificationParser;

    uses ExecutorsService;
    uses JobFactory;
    uses Command;

    exports scraper.app;

    requires scraper.core;
    requires java.compiler;
}

