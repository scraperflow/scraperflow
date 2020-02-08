package scraper.api.node;

import scraper.annotations.NotNull;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;

/**
 * Objects which implement this interface can consume and modify {@link FlowMap}s.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface NodeHook {
    void accept(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException;
    default boolean beforeHook() { return true; }
}
