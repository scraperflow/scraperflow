package scraper.api;

import scraper.annotations.NotNull;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.container.NodeContainer;

/**
 * Nodes which implement this interface only modify the FlowMap and can be tested functionally.
 */
public interface FunctionalNode extends Node {

    /** Functional node container cast and usage of process for functional nodes */
    @Override @NotNull
    default void process(@NotNull final NodeContainer<? extends Node> n, @NotNull final FlowMap o) {
        assert n instanceof FunctionalNodeContainer;
        FunctionalNodeContainer sn = ((FunctionalNodeContainer) n);
        process(sn, o);
    }

    /** Default process method should only modify and forward the modified map */
    default void process(@NotNull final FunctionalNodeContainer n, @NotNull final FlowMap o) {
        modify(n,o);
        n.forward(o);
    }

    /** Modify the given flowMap */
    void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o);

}
