package scraper.plugins.core.flowgraph.api;

import scraper.annotations.NotNull;
import scraper.api.node.Address;

/**
 * @since 1.0.0
 */
public interface ControlFlowNode {
    /** @return address of this node */
    @NotNull
    Address getAddress();
    /** Type of node */
    @NotNull
    String getType();
}
