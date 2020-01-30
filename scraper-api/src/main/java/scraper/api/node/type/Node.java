package scraper.api.node.type;

import scraper.annotations.NotNull;
import scraper.api.exceptions.NodeException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.NodeHook;
import scraper.api.specification.ScrapeInstance;

/**
 * Objects which implement this interface can consume and modify {@link FlowMap}s.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface Node {
    /**
     * Accept a {@link FlowMap}.
     * The FlowMap can be modified in the process.
     * Side-effects are possible.
     *
     * The default implementation has hooks before and hooks after processing the incoming flow map.
     *
     * @param o The FlowMap to modifiy/use in the function
     * @throws NodeException if there is a processing error during the function call
     */
    default @NotNull FlowMap accept(NodeContainer<? extends Node> n, @NotNull final FlowMap o) throws NodeException {
        for (NodeHook hook : n.beforeHooks()) { hook.accept(o); }
        FlowMap fm = process(n, o);
        for (NodeHook hook : n.afterHooks()) { hook.accept(o); }
        return fm;
    }

    @NotNull FlowMap process(NodeContainer<? extends Node> n, @NotNull final FlowMap o) throws NodeException;

    default void init(@NotNull NodeContainer<? extends Node> n, @NotNull ScrapeInstance instance) throws ValidationException {}
}
