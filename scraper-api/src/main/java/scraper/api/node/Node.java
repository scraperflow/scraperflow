package scraper.api.node;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.ControlFlow;
import scraper.api.flow.FlowMap;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The main component in a Scraper workflow specification.
 * <p>
 *     Nodes provide functionality which can be used by {@link FlowMap}s
 *     via the functional interface {@link NodeConsumer}.
 *     Many {@code FlowMaps} can use the Node concurrently.
 * </p>
 * <p>
 *     Most nodes are state-less. If stateful nodes are used in a specification,
 *     precautions have to be taken to avoid race conditions.
 * </p>
 * <p>
 *     Each node has to specify (implement) it's {@link ControlFlow} explicitly.
 *     The most basic control flow is forwarding the current {@code FlowMap} to
 *     the next node, but more complex control flows can be implemented.
 * </p>
 *
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public interface Node extends NodeConsumer, ControlFlow {

    // ==================
    // JSON Specification
    // ==================

    /** Sets the node configuration (key value pairs) and the graph its contained in */
    void setNodeConfiguration(@NotNull final Map<String, Object> nodeConfiguration, @NotNull final NodeAddress graphKey);

    /** Returns the node spec */
    @NotNull Map<String, Object> getNodeConfiguration();

    /** Returns one value for one key of the node spec */
    @Nullable Object getKeySpec(@NotNull String key);

    /** Graph this node is contained in */
    @NotNull NodeAddress getGraphKey();

    /** Job-Unique node address used for control flow */
    @NotNull NodeAddress getAddress();

    /** Job-Unique target forward node address. Defaults to the next node in the specification */
    @Nullable NodeAddress getGoTo();

    // ============
    // Control Flow
    // ============

    //-----------
    // Sequential
    //-----------

    /**
     * Forwards the flow map to another node.
     * Either next node, or the node specified by a target address given
     * Can be controlled with the forward flag
     */
    @NotNull FlowMap forward(@NotNull final FlowMap o) throws NodeException;
    @NotNull FlowMap eval(@NotNull final FlowMap o, @NotNull final NodeAddress target) throws NodeException;

    //-----------
    // Concurrent
    //-----------

    @NotNull CompletableFuture<FlowMap> forkDepend(@NotNull final FlowMap o, @NotNull final NodeAddress target);
    void forkDispatch(@NotNull final FlowMap o, @NotNull final NodeAddress target);

}
