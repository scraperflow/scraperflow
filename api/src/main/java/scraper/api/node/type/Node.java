package scraper.api.node.type;

import scraper.annotations.NotNull;
import scraper.api.exceptions.NodeException;
import scraper.api.exceptions.TemplateException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.container.NodeLogLevel;
import scraper.api.plugin.NodeHook;
import scraper.api.specification.ScrapeInstance;

/**
 * Objects which implement this interface can consume and modify {@link FlowMap}s.
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
    default @NotNull
    FlowMap accept(@NotNull final NodeContainer<? extends Node> n, @NotNull final FlowMap o) throws NodeException {
        try {
            for (NodeHook hook : n.hooks()) { hook.beforeProcess(n, o); }
            FlowMap fm = process(n, o);
            for (NodeHook hook : n.hooks()) { hook.afterProcess(n, o); }
            return n.forward(fm);
        } catch (TemplateException e) {
            n.log(NodeLogLevel.ERROR, "Template type error for {}: {}", n.getAddress(), e.getMessage());
            throw e;
        }
    }


    /** The process function which encapsulates the business logic of a node */
    @NotNull
    FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException;

    /** Initialization of a node, by default no-op */
    default void init(@NotNull NodeContainer<? extends Node> n, @NotNull ScrapeInstance instance) throws ValidationException {}
}

