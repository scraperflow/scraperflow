module scraper.annotations {
    // required for @Argument String to Object converter
    requires scraper.api;

    exports scraper.annotations.node;
    exports scraper.annotations;
    exports scraper.annotations.di;
}