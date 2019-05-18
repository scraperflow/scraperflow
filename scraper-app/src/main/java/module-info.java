import scraper.api.plugin.Addon;
import scraper.api.plugin.Hook;
import scraper.api.plugin.PreHook;

open module scraper.app {
    uses Addon;
    uses PreHook;
    uses Hook;

    requires scraper.annotations;
    requires scraper.api;
    requires scraper.core;


    requires io.github.classgraph;
    requires java.net.http;
    requires org.slf4j;
    requires scraper.utils;
}