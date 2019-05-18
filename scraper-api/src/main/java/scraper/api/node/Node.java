package scraper.api.node;

import scraper.api.exceptions.NodeException;
import scraper.api.flow.ControlFlow;
import scraper.api.flow.FlowMap;

import java.util.Map;

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

    /** Rewrites the complete node definition. Update is still needed */
    void setDefinition(Map<String, Object> newDefinition);

    // TODO #20 implement runtime updates
    /** Processes the current node definition. It it is not a valid one, reverts to the last working one and logging the error */
    void updateDefinition();

    /** Sets one key value of the node. Update is still needed */
    void setArgument(String key, Object value);


    // ============
    // Getter
    // ============

    /** Index in the process list */
    int getStageIndex();

    // TODO #21 legacy addressing
    /** Unique node label used for control flow */
    String getLabel();

    /** Can be either an parsable stage index or a stage label. Parsable stage index is tried first. */
    String getGoTo();

    // FIXME #21 new addressing
//    /** Returns own node address */
//    NodeAddress getAddress();
//
//    /** Returns the target node address. Default is 'next node' */
//    NodeAddress getTargetAddress();

    // ============
    // Control Flow
    // ============

    /** Forwards the flow map to another node. Either next node, or the node specified by a target address */
    void forward(FlowMap o) throws NodeException;

    /** Forwards map to the specified target. Target can either be a stage index or a node label. */
    void goTo(final FlowMap o, String target) throws NodeException;
    /** Forwards map to the implied target. */
    void goTo(final FlowMap o) throws NodeException;
}
