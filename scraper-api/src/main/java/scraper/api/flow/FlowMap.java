package scraper.api.flow;


import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.node.container.NodeContainer;
import scraper.api.reflect.IdentityTemplateEvaluator;
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
 */
public interface FlowMap extends IdentityTemplateEvaluator {

    //===================
    // Read
    //===================

    /** @see Map#get(Object) */
    @NotNull Optional<?> get(@NotNull String key);

    /** @see Map#get(Object) */
    @NotNull Optional<T<?>> getType(@NotNull String key);

    <K> Optional<K> getWithType(String targetKey, T<K> targetType);

    /** @see Map#size() */
    int size();

    /** @see Map#keySet() */
    @NotNull Set<String> keySet();

    // Templates Input

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


    //===================
    // Write
    //===================

    /** @see Map#remove(Object) */
    @NotNull void remove(@NotNull String key);

    /** @see Map#clear() */
    void clear();

    // Templates Output

    /** Uses the template's content and type to insert the outputObject into this FlowMap */
    <A> void output(@NotNull T<A> locationAndType, @Nullable A outputObject);
    /** Uses the template's content to insert the outputObject into this FlowMap, inferring its type */
    void output(@NotNull String location, @Nullable Object outputObject);

    //===================
    // Other
    //===================

    /** Checks if this maps contains all elements (equals method) of the other map by recursive descent */
    boolean containsElements(@NotNull FlowMap otherMap);

    //===================
    // State
    //===================

    /** If this flow map originated from another flow, returns the parent id */
    @NotNull Optional<UUID> getParentId();
    /** If this flow map originated from another flow, returns the parent's sequence number */
    @NotNull Optional<Integer> getParentSequence();

    /** Returns the sequence number of the flow. Starts with 0 */
    @NotNull int getSequence();
    /** Increases the sequence number of this flow */
    @NotNull void nextSequence();

    /** Returns the unique id of this flow */
    @NotNull UUID getId();

    /** Copies this FlowMap */
    FlowMap copy();

    /** Creates a new flow with a new ID */
    FlowMap newFlow();

}
