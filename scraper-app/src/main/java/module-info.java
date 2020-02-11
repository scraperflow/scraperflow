import scraper.api.plugin.Addon;
import scraper.api.plugin.Hook;
import scraper.api.plugin.NodeHook;

open module scraper.app {
    uses Addon;
    uses Hook;
    uses NodeHook;

    requires scraper.annotations;
    requires scraper.api;
    requires scraper.core;


    requires io.github.classgraph;
    requires java.net.http;
    requires org.slf4j;
    requires scraper.utils;
}