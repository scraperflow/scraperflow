package scraper.api;

import scraper.annotations.NotNull;

import java.util.concurrent.ExecutorService;

/**
 * Service managing the execution and scheduling of flows
 */
public interface ExecutorsService {

    /**
     * Returns the executor service for given group and job.
     * If the service for the group was not instantiated yet,
     * creates a new ExecutorService with the specified limit of threads
     */
    @NotNull
    ExecutorService getService(@NotNull String jobName, @NotNull String group, @NotNull Integer count);
}
