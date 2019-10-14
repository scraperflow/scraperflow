package scraper.api.specification;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.node.NodeAddress;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Specification of a Scraper .scrape workflow.
 *
 * @since 1.0.0
 */
public interface ScrapeSpecification {

    /** Name of the scrape spec */
    @NotNull String getName();

    /** Returns the path to the .scrape file which will be used as the workflow specification for this workflow */
    @NotNull Path getScrapeFile();

    /** label -> Graph definitions */
    @NotNull Map<NodeAddress, List<Map<String, Object>>> getGraphs();

    /** Any number of added base paths (with command line arguments) to search for arguments, dependencies, imports, fragments */
    @NotNull List<Path> getPaths();

    /** Node dependency reference */
    @Nullable String getDependencies();

    /** Arguments used */
    @NotNull List<String> getArguments();

    /** .scrape file reference -> exported labels */
    @NotNull Map<String, List<NodeAddress>> getImports();

    /** Entry points */
    @NotNull NodeAddress getEntry();

    /** Global node configuration */
    @NotNull Map<String, Map<String, Object>> getGlobalNodeConfigurations();
}
