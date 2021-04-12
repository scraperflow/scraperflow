package scraper.api.node.container;

import scraper.annotations.NotNull;
import scraper.api.flow.FlowMap;
import scraper.api.flow.IdentityTemplateEvaluator;
import scraper.api.node.Address;
import scraper.api.node.GraphAddress;
import scraper.api.node.NodeAddress;
import scraper.api.node.NodeInitializable;
import scraper.api.node.type.Node;
import scraper.api.plugin.NodeHook;
import scraper.api.specification.ScrapeInstance;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * The main component in a Scraper workflow specification.
 * <p>
 *     Nodes provide functionality which can be used by {@link FlowMap}s
 *     via the functional interface {@link Node}.
 *     Many {@code FlowMaps} can access the Node concurrently.
 * </p>
 * <p>
 *     Most nodes are state-less. If stateful nodes are used in a specification,
 *     precautions have to be taken to avoid race conditions.
 * </p>
 */
public interface NodeContainer<NODE> extends NodeInitializable, IdentityTemplateEvaluator {

    // ==================
    // Specification
    // ==================

    /** Sets the node configuration (key value pairs) and the graph its contained in */
    void setNodeConfiguration(@NotNull Map<String, ?> nodeConfiguration, @NotNull String instance, @NotNull String graph);

    /** Returns the node spec */
    @NotNull
    Map<String, ?> getNodeConfiguration();

    /** Returns one value for one key of the node spec */
    @NotNull
    Optional<?> getKeySpec(@NotNull String key);

    /** Graph this node is contained in */
    @NotNull
    GraphAddress getGraphKey();

    /** Job-Unique node address used for control flow */
    @NotNull
    NodeAddress getAddress();

    /** Returns the associated job instance this node container is used in */
    @NotNull
    ScrapeInstance getJobInstance();

    /** Returns the executor service to launch threads outside the workflow specification */
    @NotNull
    ExecutorService getService();

    // ============
    // Control Flow
    // ============

    /**
     * Job-Unique target forward address.
     * Defaults to the next node in the specification.
     * Returns the empty optional if no goTo node is found.
     */
    @NotNull
    Optional<NodeContainer<? extends Node>> getGoTo();

    /** Hooks which are executed after processing a flow map */
    @NotNull
    Collection<NodeHook> hooks();

    /** If forwarding is enabled or not */
    boolean isForward();

    //-----------
    // Forward
    //-----------

    /**
     * Forwards the flow map to another node.
     * Either next node, or the node specified by a target address given
     * Can be controlled with the forward flag
     */
    void forward(@NotNull FlowMap o);

    /**
     * Forwards the flow map to another node with specified target address.
     * Either next node, or the node specified by a target address given
     * Is not controlled by the forward flag
     */
    void forward(@NotNull FlowMap o, @NotNull Address target);

    //-----------
    // Implementation Container
    //-----------

    @NotNull
    NODE getC();

    //-----------
    // Address Parsing
    //-----------

    @NotNull
    Address addressOf(@NotNull String representation);

    //-----------
    // Logging
    //-----------

    void log(@NotNull NodeLogLevel trace, @NotNull String s, @NotNull Object... args);
}
