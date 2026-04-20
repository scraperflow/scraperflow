package scraper.api;

import scraper.annotations.NotNull;

/**
 * Objects which implement this interface can consume and modify {@link FlowMap}s.
 */
@FunctionalInterface
public interface NodeHook extends Command, Comparable<NodeHook>  {
    void beforeProcess(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o);
    default void afterProcess(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {}

    /** Execution order */
    default int order() { return 100; }

    @Override
    default int compareTo(NodeHook other) {
        return Integer.compare(order(), other.order());
    }
}
