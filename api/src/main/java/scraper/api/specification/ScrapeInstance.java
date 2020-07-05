package scraper.api.specification;

import scraper.annotations.NotNull;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.IdentityTemplateEvaluator;
import scraper.api.node.Address;
import scraper.api.node.GraphAddress;
import scraper.api.node.InstanceAddress;
import scraper.api.node.NodeAddress;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.plugin.NodeHook;
import scraper.api.service.ExecutorsService;
import scraper.api.service.FileService;
import scraper.api.service.HttpService;
import scraper.api.service.ProxyReservation;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Provides scrape specification functions needed during runtime.
 */
public interface ScrapeInstance extends IdentityTemplateEvaluator {

    // ===========
    // Initialization and route building of the instances
    // ===========

    /** Adding a route mapping from address to node */
    void addRoute(@NotNull Address address, @NotNull NodeContainer<? extends Node> node);

    /** Sets the special entry address. Routes which point to this instance start with this node */
    void setEntry(@NotNull GraphAddress address, @NotNull NodeContainer<? extends Node> nn);

    /** Initializes the workflow after every route was added */
    void init() throws ValidationException;

    // ===========
    // Getter
    // ===========

    /** Returns the name of this workflow instance */
    @NotNull
    String getName();

    /** Specification used for this instance */
    @NotNull
    ScrapeSpecification getSpecification();

    /** Imported instance addresses, if any. Includes nested imported instances. */
    @NotNull
    Map<InstanceAddress, ScrapeInstance> getImportedInstances();

    /** Hooks before/after a flow map has been processed */
    @NotNull
    Collection<NodeHook> getHooks();

    /** Returns the entry node, if any */
    @NotNull
    Optional<NodeContainer<? extends Node>> getEntry();

    /** Gets a node at absolute target address (has to exist, NodeAddress can't be generated) */
    @NotNull
    NodeContainer<? extends Node> getNode(@NotNull NodeAddress target);

    /** Gets a node at a relative target address maybe */
    @NotNull
    Optional<NodeContainer<? extends Node>> getNode(@NotNull Address target);

    /** Gets a node at a target address denoted by a string representation ({@code <instance.graph.node>}) maybe */
    @NotNull
    Optional<NodeContainer<? extends Node>> getNode(@NotNull String targetRepresentation);

    /** Initial arguments passed via command line or args files */
    @NotNull
    Map<String, Object> getEntryArguments();

    /** Flat address mapping to all known nodes including nodes in nested imported instances */
    @NotNull
    Map<Address, NodeContainer<? extends Node>> getRoutes();

    // ===========
    // Helper
    // ===========

    // Cross-cutting concerns services
    @NotNull
    ExecutorsService getExecutors();
    @NotNull
    HttpService getHttpService();
    @NotNull
    ProxyReservation getProxyReservation();
    @NotNull
    FileService getFileService();
}
