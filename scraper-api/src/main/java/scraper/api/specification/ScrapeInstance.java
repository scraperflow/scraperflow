package scraper.api.specification;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.node.Node;
import scraper.api.node.NodeAddress;
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
    @NotNull Map<String, Object> getInitialArguments();

    /** 'all' configuration */
    @NotNull Map<String, Map<String, Object>> getGlobalNodeConfigurations();

    /** Returns the name of this workflow instance */
    String getName();

    /** Returns the description of this workflow instance */
    String getDescription();

    /** Returns instantiated node in the flow. main flow has precedence over fragment flows. Throws a runtime exception if address is not found */
    @NotNull Node getNode(@NotNull NodeAddress target);

    @Nullable NodeAddress getForwardTarget(@NotNull NodeAddress origin);

    /** Returns the main flow */
    @NotNull List<Node> getMainFlow();

    /** Returns the fragment flows */
    @NotNull List<List<Node>> getFragmentFlows();

    // Cross-cutting concerns services
    ExecutorsService getExecutors();
    HttpService getHttpService();
    ProxyReservation getProxyReservation();
    FileService getFileService();

}
