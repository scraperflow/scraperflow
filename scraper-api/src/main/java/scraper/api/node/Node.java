package scraper.api.node;

import scraper.api.exceptions.NodeException;
import scraper.api.flow.ControlFlow;
import scraper.api.flow.FlowMap;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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
public interface Node extends NodeConsumer, ControlFlow {

    // ==================
    // JSON Specification
    // ==================

    /** Returns one value for one key of the node spec */
    Object getKeySpec(String key);

    /** Returns the complete node definition */
    Map<String, Object> getNodeJsonSpec();


    /** Job-Unique node address used for control flow */
    NodeAddress getAddress();


    /** Job-Unique target node address */
    NodeAddress getTarget();

    // ============
    // Control Flow
    // ============

    /**
     * Forwards the flow map to another node.
     * Either next node, or the node specified by a target address at key 'goTo'
     * If dependent is false, then this function returns immediately if the queue allows for creation of more flows
     * and returns null as the result
     */


    /**
     * Forwards the flow map to another node.
     * Either next node, or the node specified by a target address given
     * If dependent is false, then this function returns immediately if the queue allows for creation of more flows
     * and returns null as the result
     *
     */
    FlowMap forward(FlowMap o, NodeAddress target) throws NodeException;
    default FlowMap forward(FlowMap o) throws NodeException { return forward(o, getTarget()); }

    CompletableFuture<FlowMap> forkDepend(FlowMap o, NodeAddress target);
    default CompletableFuture<FlowMap> forkDepend(FlowMap o) { return forkDepend(o, getTarget()); }

    void forkDispatch(FlowMap o, NodeAddress target);
    default void forkDispatch(FlowMap o) { forkDispatch(o, getTarget()); }
}
