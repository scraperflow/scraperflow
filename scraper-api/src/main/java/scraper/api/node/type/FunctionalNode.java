package scraper.api.node.type;

import scraper.annotations.NotNull;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.Node;

/**
 * Nodes which implement this interface only modify the FlowMap and can be tested functionally.
 *
 * @since 1.0.0
 */
public interface FunctionalNode extends Node {
    /** Modify the given flowMap */
    void modify(FlowMap o) throws NodeException;

    /** Default accept method should only modify and forward the modified map */
    @NotNull
    default FlowMap process(@NotNull final FlowMap o) throws NodeException {
        modify(o);
        return forward(o);
    }
}
