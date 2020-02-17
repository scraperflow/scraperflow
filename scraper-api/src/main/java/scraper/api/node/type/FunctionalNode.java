package scraper.api.node.type;

import scraper.annotations.NotNull;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.container.NodeContainer;

/**
 * Nodes which implement this interface only modify the FlowMap and can be tested functionally.
 */
public interface FunctionalNode extends Node {

    /** Functional node container cast and usage of process for functional nodes */
    @Override @NotNull
    default FlowMap process(@NotNull final NodeContainer n, @NotNull final FlowMap o) throws NodeException {
        assert n instanceof FunctionalNodeContainer;
        FunctionalNodeContainer sn = ((FunctionalNodeContainer) n);
        return process(sn, o);
    }

    /** Default process method should only modify and forward the modified map */
    default FlowMap process(@NotNull final FunctionalNodeContainer n, @NotNull final FlowMap o) throws NodeException {
        modify(n,o);
        return o;
    }

    /** Modify the given flowMap */
    void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) throws NodeException;

}
