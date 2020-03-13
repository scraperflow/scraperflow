import scraper.api.node.type.Node;
import scraper.nodes.unstable.api.telegram.TelegramNode;

open module scraper.nodes.unstable {
    requires scraper.annotations;
    requires scraper.api;
    requires scraper.core;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    requires java.desktop;

    exports scraper.nodes.unstable.api.telegram;
    exports scraper.nodes.unstable.pass.dialog;

    // FIXME why is this needed so that reflections can find all nodes?
    provides Node with TelegramNode;
}
