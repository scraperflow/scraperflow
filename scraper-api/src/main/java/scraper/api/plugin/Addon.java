package scraper.api.plugin;

import scraper.annotations.NotNull;
import scraper.api.di.DIContainer;

/**
 * An addon modifies and/or provides functionality to the Scraper framework.
 * <p>
 *     When it is loaded at startup, it can use other dependencies via the dependency injection framework.
 * </p>
 *
 * @since 1.0.0
 */
public interface Addon {
    /** Loads and initializes the addon with already loaded dependencies */
    void load(@NotNull DIContainer loadedDependencies);
}
