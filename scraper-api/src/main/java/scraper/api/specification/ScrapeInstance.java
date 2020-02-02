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

    void init() throws ValidationException;

    /** Specification used for this instance */
    @NotNull ScrapeSpecification getSpecification();

    /** Imported instances */
    @NotNull Map<InstanceAddress, ScrapeInstance> getImportedInstances();

    /** Returns the name of this workflow instance */
    @NotNull String getName();

    // Cross-cutting concerns services
    @NotNull ExecutorsService getExecutors();
    @NotNull HttpService getHttpService();
    @NotNull ProxyReservation getProxyReservation();
    @NotNull FileService getFileService();

    void setEntry(GraphAddress address, NodeContainer<? extends Node> nn);
    /** Returns the entry node */
    @NotNull NodeContainer<? extends Node> getEntry();

    /** Gets a node at absolute target address (has to exist) */
    @NotNull NodeContainer<? extends Node> getNode(@NotNull NodeAddress target);

    /** Gets a node at a relative target address maybe */
    @NotNull Optional<NodeContainer<? extends Node>> getNode(@NotNull Address target);

    void addRoute(Address address, NodeContainer<? extends Node> node);

    @NotNull Map<String, Object> getEntryArguments();
}
