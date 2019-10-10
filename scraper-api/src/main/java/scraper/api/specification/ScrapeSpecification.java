package scraper.api.specification;

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
    String getName();

    /** Returns the path to the .scrape file which will be used as the workflow specification for this workflow */
    Path getScrapeFile();

    /** Any number of added base paths (with command line arguments) to search for arguments, dependencies, imports, fragments */
    List<Path> getPaths();

    /** Node dependency reference */
    String getDependencies();

    /** Arguments used */
    List<String> getArguments();

    /** Label -> .scrape file references */
    Map<String, String> getImports();

    /** Entry points */
    NodeAddress getEntry();

    /** label -> Graph definitions */
    Map<NodeAddress, List<Map<String, Object>>> getGraphs();

    Map<String, Map<String, Object>> getGlobalNodeConfigurations();

//    /** Tries to instantiate the scrape job */
//    ScrapeInstance getInstance() throws ValidationException;
}
