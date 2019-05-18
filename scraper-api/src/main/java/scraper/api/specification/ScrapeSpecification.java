package scraper.api.specification;

import java.util.Collection;
import java.util.List;

/**
 * Specification of a Scrape workflow process.
 *
 * @since 1.0.0
 */
public interface ScrapeSpecification {

    /** Returns the path to the .scrape file which will be used as the workflow specification for this workflow */
    String getScrapeFile();

    /** Returns the base path of this workflow */
    String getBasePath();

    /** Name of the scrape job */
    String getName();

    /** Any number of argument files */
    List<String> getArgumentFiles();

    /** Any number of added base paths */
    List<String> getPaths();

    /** Returns the path to the node dependency file which will be used for versioning this workflow */
    String getNodeDependencyFile();

    /** Known fragment paths */
    @Deprecated // TODO remove?
    Collection<String> getFragmentFolders();
}
