package scraper.api.specification;

import scraper.annotations.NotNull;
import scraper.api.exceptions.ValidationException;
import scraper.api.node.Address;
import scraper.api.node.GraphAddress;
import scraper.api.node.InstanceAddress;
import scraper.api.node.NodeAddress;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.service.ExecutorsService;
import scraper.api.service.FileService;
import scraper.api.service.HttpService;
import scraper.api.service.ProxyReservation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    /** ? */
    @NotNull Map<GraphAddress, List<NodeContainer<? extends Node>>> getGraphs();

    /** ? */
    @NotNull List<NodeContainer<? extends Node>> getEntryGraph();

    /** ? */
    @NotNull List<NodeContainer<? extends Node>> getGraph(@NotNull GraphAddress label);

    /** Imported instances */
    @NotNull Map<InstanceAddress, ScrapeInstance> getImportedInstances();

    void init() throws ValidationException;

    // Cross-cutting concerns services
    @NotNull ExecutorsService getExecutors();
    @NotNull HttpService getHttpService();
    @NotNull ProxyReservation getProxyReservation();
    @NotNull FileService getFileService();

    /** Returns instantiated node in the flow. main flow has precedence over fragment flows. Throws a runtime exception if address is not found */
    @NotNull Optional<NodeContainer<? extends Node>> getNodeAbsolute(@NotNull NodeAddress target);
    @NotNull Optional<NodeContainer<? extends Node>> getNodeRelative(@NotNull NodeAddress address, @NotNull Address addressOf);
}
