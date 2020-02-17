package scraper.api.plugin;

import scraper.annotations.NotNull;
import scraper.api.di.DIContainer;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;

import java.util.Map;

/**
 * After parsing and instantiating Scrape specifications, all hooks found on the class/module path will get executed
 * and provided access to all specifications and instances of workflows.
 * <p>
 * Additionally, dependencies can be used.
 */
public interface Hook {
    /** Executes the hook and provides arguments, dependencies, and a ScrapeSpecification to ScrapeInstance map */
    void execute(@NotNull DIContainer dependencies, @NotNull String[] args,
                 @NotNull Map<ScrapeSpecification, ScrapeInstance> scraper) throws Exception;
}
