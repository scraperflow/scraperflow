package scraper.api.plugin;

import scraper.annotations.NotNull;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;

/**
 * Objects which implement this interface can consume and modify {@link FlowMap}s.
 */
@FunctionalInterface
public interface NodeHook {
    void beforeProcess(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException;
    default void afterProcess(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {}
}
