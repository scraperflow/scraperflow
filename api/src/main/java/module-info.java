module scraper.api {
    exports scraper.annotations;

    requires transitive java.net.http;

    exports scraper.api;
}