package scraper.api.flow;


import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.node.container.NodeContainer;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.Map;
import java.util.Optional;
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

    // Templates Input

    /** Evaluates the given template with this flowmap's content, enforces non-null return */
    @NotNull
    <A> A eval(@NotNull T<A> template);
    /** Evaluates the given template with this flowmap's content, returns empty optional if template is null */
    @NotNull
    <A> Optional<A> evalMaybe(@NotNull T<A> template);
    /** Evaluates the given template where templates are replaced by their string identity, i.e. no evaluation at all. Enforces non-null return */
    @NotNull
    <A> A evalIdentity(@NotNull T<A> template);
    /** Evaluates the given template where templates are replaced by their string identity, i.e. no evaluation at all */
    @NotNull
    <A> Optional<A> evalIdentityMaybe(@NotNull T<A> template);

    /** Evaluates the given location template with this flowmap's content, enforces non-null return */
    @NotNull
    <A> String evalLocation(@NotNull L<A> template);
    /** Evaluates the given location template with this flowmap's content, returns empty optional if template is null */
    @NotNull
    <A> Optional<String> evalLocationMaybe(@NotNull L<A> template);

    // Templates Output
    /** Uses the template's content and type to insert the outputObject into this FlowMap */
    <A> void output(@NotNull L<A> locationAndType, @Nullable A outputObject);

    //===================
    // State
    //===================

    /** If this flow map originated from another flow, returns the parent id */
    @NotNull
    Optional<UUID> getParentId();
    /** If this flow map originated from another flow, returns the parent's sequence number */
    @NotNull
    Optional<Integer> getParentSequence();

    /** Returns the sequence number of the flow. Starts with 0 */
    int getSequence();
    /** Increases the sequence number of this flow */
    void nextSequence();

    /** Returns the unique id of this flow */
    @NotNull
    UUID getId();

    /** Copies this FlowMap */
    @NotNull
    FlowMap copy();

    /** Creates a new flow with a new ID */
    @NotNull
    FlowMap newFlow();

}
