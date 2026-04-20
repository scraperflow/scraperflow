import scraper.api.Addon;
import scraper.api.Hook;

import scraper.test.providers.TestAddon;
import scraper.test.providers.TestHook;
import scraper.test.providers.PluginHook;

/**
 * Test-only module that registers fake {@link Addon} and {@link Hook}
 * providers so {@code DependencyInjectionUtilTest} can verify
 * ServiceLoader-based plugin discovery.
 */
module scraper.core.testproviders {
    requires scraper.api;

    exports scraper.test.providers;

    provides Addon with TestAddon;
    provides Hook with TestHook, PluginHook;
}
