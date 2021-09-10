import scraper.api.Addon;
import scraper.api.Hook;
import scraper.test.addons.TestAddon;
import scraper.test.hooks.PluginHook;
import scraper.test.hooks.TestHook;

open module scraper.core.test {
    requires scraper.api;
    requires org.junit.jupiter.api;
    requires scraper.core;
    requires org.junit.jupiter.params;


    provides Addon with TestAddon;
    provides Hook with PluginHook, TestHook;
}