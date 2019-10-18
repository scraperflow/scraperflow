package scraper.api.specification;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.exceptions.ValidationException;
import scraper.api.node.*;
import scraper.api.service.ExecutorsService;
import scraper.api.service.FileService;
import scraper.api.service.HttpService;
import scraper.api.service.ProxyReservation;

import java.util.Date;
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
    @NotNull String getName();

    /** Returns instantiated node in the flow. main flow has precedence over fragment flows. Throws a runtime exception if address is not found */
    @NotNull Node getNode(@NotNull Address target);

    @Nullable Address getForwardTarget(@NotNull NodeAddress origin);

    /** ? */
    @NotNull Map<GraphAddress, List<Node>> getGraphs();

    /** ? */
    @NotNull List<Node> getEntryGraph();

    /** ? */
    @NotNull List<Node> getGraph(@NotNull GraphAddress label);

    /** Imported instances */
    @NotNull Map<InstanceAddress, ScrapeInstance> getImportedInstances();

    void init() throws ValidationException;

    // Cross-cutting concerns services
    @NotNull ExecutorsService getExecutors();
    @NotNull HttpService getHttpService();
    @NotNull ProxyReservation getProxyReservation();
    @NotNull FileService getFileService();

}
