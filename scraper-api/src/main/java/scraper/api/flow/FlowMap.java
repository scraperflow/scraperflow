package scraper.api.flow;


import scraper.api.node.Node;

import java.util.Map;
import java.util.Set;

/**
 * A map-like data structure which 'flows' through nodes in the control flow order.
 * <p>
 *     A FlowMap can be modified by each node and either passed on or be copied to other nodes.
 *
 * @see Node
 * @see Map
 * @since 1.0.0
 */
public interface FlowMap {
    /** @see Map#put(Object, Object) */
    Object put(String key, Object value);

    /** @see Map#putAll(Map) */
    void putAll(Map<String, Object> m);

    /** @see Map#remove(Object) */
    Object remove(String key);

    /** @see Map#get(Object) */
    Object get(String key);

    /** @see Map#size() */
    int size();

    /** @see Map#clear() */
    void clear();

    /** @see Map#keySet() */
    Set<String> keySet();

    /** @see Map#getOrDefault(Object, Object) */
    Object getOrDefault(Object key, Object defaultObjectalue);

    /** Checks if this maps contains all elements of the other map by recursive descent */
    boolean containsElements(FlowMap expectedOutput);

    /** Returns the current state of this flow */
    FlowState getFlowState();

    /** Sets the current state of this flow */
    void setFlowState(FlowState newState);

    /** Will get removed from this interface */
    // TODO #19 refactor this method out of this interface
    @Deprecated
    Map<String,Object> getMap();
}
