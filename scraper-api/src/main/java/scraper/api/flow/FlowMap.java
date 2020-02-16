package scraper.api.flow;


import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.node.container.NodeContainer;
import scraper.api.reflect.T;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A map-like data structure which 'flows' through nodes in the control flow order.
 * <p>
 * A FlowMap can be modified by each node and either passed on or be copied to other nodes.
 *
 * @see NodeContainer
 * @see Map
 * @since 1.0.0
 */
public interface FlowMap {

    //===================
    // Read
    //===================

    /** @see Map#get(Object) */
    @NotNull Optional<Object> get(@NotNull String key);

    /** @see Map#getOrDefault(Object, Object) */
    @NotNull Object getOrDefault(@NotNull Object key, @NotNull Object defaultObjectValue);

    /** @see Map#size() */
    int size();

    /** @see Map#keySet() */
    @NotNull Set<String> keySet();


    //===================
    // Write
    //===================

    /** @see Map#put(Object, Object) */
    @NotNull Optional<?> put(@NotNull String key, @NotNull Object value);

    /** @see Map#putAll(Map) */
    void putAll(@NotNull Map<String, Object> m);

    /** @see Map#remove(Object) */
    @NotNull Optional<Object> remove(@NotNull String key);

    /** @see Map#clear() */
    void clear();


    //===================
    // Other
    //===================

    /** Checks if this maps contains all elements (equals method) of the other map by recursive descent */
    boolean containsElements(@NotNull FlowMap otherMap);

    //===================
    // State
    //===================

    /** If this flow map originated from another flow */
    @NotNull Optional<UUID> getParentId();
    @NotNull Optional<Integer> getParentSequence();

    /** Returns the sequence number of the flow. Starts with 0 */
    @NotNull int getSequence();
    @NotNull void nextSequence();

    /** Returns the unique id of this flow */
    @NotNull UUID getId();


    //===================
    // Templates
    //===================

    // Input

    /** Evaluates the given template with this flowmap's content, enforces non-null return */
    @NotNull <A> A eval(@NotNull T<A> template);
    /** Evaluates the given template with this flowmap's content, returns empty optional if template is null */
    @NotNull <A> Optional<A> evalMaybe(@NotNull T<A> template);
    /** Evaluates the given template where templates are replaced by their string identity, i.e. no evaluation at all. Enforces non-null return */
    @NotNull <A> A evalIdentity(@NotNull T<A> template);
    /** Evaluates the given template where templates are replaced by their string identity, i.e. no evaluation at all */
    @NotNull <A> Optional<A> evalIdentityMaybe(@NotNull T<A> template);
    /** Evaluates the given template with this flowmap's content, returns default eval if return would be null */
    @NotNull <A> A evalOrDefault(@NotNull T<A> template, @NotNull A defaultEval);

    // Output

    <A> void output(@NotNull T<A> locationAndType, @Nullable A outputObject);

    FlowMap copy();
}
