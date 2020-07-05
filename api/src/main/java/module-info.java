module scraper.api {
    exports scraper.annotations.node;
    exports scraper.annotations;
    exports scraper.annotations.di;

    requires transitive java.net.http;

    exports scraper.api.exceptions;
    exports scraper.api.node;
    exports scraper.api.node.type;
    exports scraper.api.plugin;
    exports scraper.api.specification;
    exports scraper.api.service;
    exports scraper.api.flow;
    exports scraper.api.di;
    exports scraper.api.service.proxy;
    exports scraper.api.template;
    exports scraper.api.node.container;
}