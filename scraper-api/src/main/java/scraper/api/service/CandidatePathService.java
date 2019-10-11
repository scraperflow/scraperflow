package scraper.api.service;

import scraper.annotations.NotNull;

import java.util.Set;

/**
 * Manages known paths to look for files. Used to find .scrape, .args, .yml files.
 *
 * @since 1.0.0
 */
public interface CandidatePathService {
    /** Add a path to the service */
    void addPath(@NotNull String path);
    /** Get all currently known paths */
    @NotNull Set<String> getCandidatePaths();
}
