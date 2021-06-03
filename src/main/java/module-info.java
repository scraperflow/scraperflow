import scraper.api.Addon;
import scraper.api.Hook;
import scraper.api.NodeHook;

module scraper.app {
    uses Addon;
    uses Hook;
    uses NodeHook;

    exports scraper.app;

    requires scraper.core;
    requires java.compiler;
}

