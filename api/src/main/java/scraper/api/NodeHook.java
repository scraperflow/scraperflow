package scraper.api.plugin;

import scraper.annotations.NotNull;
import scraper.api.FlowMap;
import scraper.api.NodeContainer;
import scraper.api.Node;

/**
 * Objects which implement this interface can consume and modify {@link FlowMap}s.
 */
@FunctionalInterface
public interface NodeHook {
    void beforeProcess(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o);
    default void afterProcess(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {}
}
