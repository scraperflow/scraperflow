package scraper.api.specification;

import scraper.api.node.Node;
import scraper.api.service.ExecutorsService;
import scraper.api.service.FileService;
import scraper.api.service.HttpService;
import scraper.api.service.ProxyReservation;

import java.util.List;
import java.util.Map;

/**
 * Provides scrape specification functions needed during runtime.
 *
 * @since 1.0.0
 */
public interface ScrapeInstance {
    /** Returns the initial arguments parsed from .args files */
    Map<String, Object> getInitialArguments();

    /** Returns instantiated node in the workflow */
    Node getProcessNode(String target);

    /** Returns the name of this workflow instance */
    String getName();

    /** Returns the initial definition of the labeled node in the workflow */
    Map<String, Object> getProcessNodeDefinition(String target);

    /** Returns the complete process */
    List<Node> getJobProcess();


    Object getProcessKey(int stageIndex, String key);
    Map<String, Object> getProcessKeys(int stageIndex);

    String getDescription();

    Map<String, Map<String, Object>> getAll();

    ExecutorsService getExecutors();
    HttpService getHttpService();
    ProxyReservation getProxyReservation();
    FileService getFileService();
}
