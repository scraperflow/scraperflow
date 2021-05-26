import scraper.api.plugin.Addon;
import scraper.api.plugin.Hook;
import scraper.api.plugin.NodeHook;

module scraper.app {
    uses Addon;
    uses Hook;
    uses NodeHook;

    exports scraper.app;

    requires scraper.core;
    requires java.compiler;
}

