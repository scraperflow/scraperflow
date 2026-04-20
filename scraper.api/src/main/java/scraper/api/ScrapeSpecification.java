package scraper.api;

import scraper.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Specification of a Scraper .scrape workflow.
 */
public interface ScrapeSpecification {

    /** Name of the scrape spec */
    @NotNull
    String getName();

    /** Returns the path to the .scrape file which will be used as the workflow specification for this workflow */
    @NotNull
    Path getScrapeFile();

    /** label -> Graph definitions */
    @NotNull
    Map<String, List<Map<String, Object>>> getGraphs();

    /** Any number of added base paths (with command line arguments) to search for arguments, dependencies, imports, fragments */
    @NotNull
    List<Path> getPaths();

    /** Node dependency reference */
    @NotNull
    Optional<String> getDependencies();

    /** Arguments used */
    @NotNull
    List<String> getArguments();

    /** spec `name -> ScrapeImportSpecification */
    @NotNull
    Map<String, ScrapeImportSpecification> getImports();

    /** Entry graph address */
    @NotNull
    String getEntry();

    /** Global node configuration */
    @NotNull
    Map<String, Map<String, Object>> getGlobalNodeConfigurations();


    ScrapeSpecification with(String arg);
}
