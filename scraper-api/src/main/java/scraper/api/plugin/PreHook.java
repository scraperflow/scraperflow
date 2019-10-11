package scraper.api.plugin;

import scraper.annotations.NotNull;
import scraper.api.di.DIContainer;

/**
 * Before parsing and instantiating Scrape specifications, all pre hooks found on the class/module path will get executed
 * and provided access to dependencies and passed arguments.
 *
 * @since 1.0.0
 * @see Hook
 */
public interface PreHook {
    /** Executes the pre hook and provides command-line arguments and dependencies */
    void execute(@NotNull final DIContainer dependencies, @NotNull final String[] args) throws Exception;
}
