package scraper.api.flow;


import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
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
    @Nullable Object put(@NotNull String key, @NotNull Object value);

    /** @see Map#putAll(Map) */
    void putAll(@NotNull Map<String, Object> m);

    /** @see Map#remove(Object) */
    @Nullable Object remove(@NotNull String key);

    /** @see Map#get(Object) */
    @Nullable Object get(@NotNull String key);

    /** @see Map#size() */
    int size();

    /** @see Map#clear() */
    void clear();

    /** @see Map#keySet() */
    @NotNull Set<String> keySet();

    /** @see Map#getOrDefault(Object, Object) */
    @NotNull Object getOrDefault(@NotNull Object key, @NotNull Object defaultObjectValue);

    /** Checks if this maps contains all elements (equals method) of the other map by recursive descent */
    boolean containsElements(@NotNull FlowMap otherMap);

    /** State of this flow. Useful for debugging purposes. Amount of tracking increases with log level of the nodes accessed in the flow. */
    @NotNull FlowHistory getFlowHistory();
}
