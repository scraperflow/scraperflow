open module scraper.plugins.core {
    requires scraper.annotations;
    requires scraper.api;
    requires scraper.core;
    requires scraper.utils;

//    // FIXME why is this needed so that reflections can find all nodes?
//    provides Node with EchoNode;
}
