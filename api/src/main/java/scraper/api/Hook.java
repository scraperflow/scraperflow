package scraper.api;

import scraper.annotations.NotNull;

import java.util.Map;

/**
 * After parsing and instantiating Scrape specifications, all hooks found on the class/module path will get executed
 * and provided access to all specifications and instances of workflows.
 * <p>
 * Additionally, dependencies can be used.
 */
public interface Hook extends Command, Comparable<Hook> {
    /** Executes the hook and provides arguments, dependencies, and a ScrapeSpecification to ScrapeInstance map */
    void execute(@NotNull DIContainer dependencies, @NotNull String[] args,
                 @NotNull Map<ScrapeSpecification, ScrapeInstance> scraper) throws Exception;

    /** Execution order */
    default int order() { return 100; }

    /** Execution order */
    default boolean preValidation() { return false; }

    @Override
    default int compareTo(Hook other) {
        return Integer.compare(order(), other.order());
    }
}
