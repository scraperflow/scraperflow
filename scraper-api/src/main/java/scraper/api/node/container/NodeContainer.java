package scraper.api.node.container;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.*;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * The main component in a Scraper workflow specification.
 * <p>
 *     Nodes provide functionality which can be used by {@link FlowMap}s
 *     via the functional interface {@link NodeConsumer}.
 *     Many {@code FlowMaps} can access the Node concurrently.
 * </p>
 * <p>
 *     Most nodes are state-less. If stateful nodes are used in a specification,
 *     precautions have to be taken to avoid race conditions.
 * </p>
 *
 * @since 1.0.0
 */
public interface NodeContainer<NODE> extends NodeInitializable {

    // ==================
    // Specification
    // ==================

    /** Sets the node configuration (key value pairs) and the graph its contained in */
    void setNodeConfiguration(@NotNull final Map<String, Object> nodeConfiguration, @NotNull String instance, @NotNull final String graphKey);

    /** Returns the node spec */
    @NotNull Map<String, Object> getNodeConfiguration();

    /** Returns one value for one key of the node spec */
    @Nullable Object getKeySpec(@NotNull String key);

    /** Graph this node is contained in */
    @NotNull GraphAddress getGraphKey();

    /** Job-Unique node address used for control flow */
    @NotNull NodeAddress getAddress();

    // ============
    // Control Flow
    // ============


    /** Job-Unique target forward address. Defaults to the next node in the specification */
    @NotNull Optional<NodeContainer<? extends Node>> getGoTo();

    @NotNull Collection<NodeHook> beforeHooks();

    @NotNull Collection<NodeHook> afterHooks();

    boolean isForward();

    //-----------
    // Sequential
    //-----------

    /**
     * Forwards the flow map to another node.
     * Either next node, or the node specified by a target address given
     * Can be controlled with the forward flag
     */
    @NotNull FlowMap forward(@NotNull final FlowMap o) throws NodeException;

    /**
     * Forwards the flow map to another node.
     * Either next node, or the node specified by a target address given
     * Is not controlled by the forward flag
     */
    @NotNull FlowMap eval(@NotNull final FlowMap o, @NotNull final Address target) throws NodeException;

    //-----------
    // Concurrent
    //-----------

    /**
     * Copies and dispatches a flow to another target address.
     * Returns a future which can be used to get the output flow of the dispatched flow.
     * This call only returns if the service pool has available space for another flow.
     */
    @NotNull CompletableFuture<FlowMap> forkDepend(@NotNull final FlowMap o, @NotNull final Address target);

    /**
     * Copies and dispatches a flow to another target address.
     * Does not wait for the other flow to finish.
     * This call only returns if the service pool has available space for another flow.
     */
    void forkDispatch(@NotNull final FlowMap o, @NotNull final Address target);

    NODE getC();

    void log(NodeLogLevel trace, String s, Object... args);

    ScrapeInstance getJobInstance();

    ExecutorService getService();
}
