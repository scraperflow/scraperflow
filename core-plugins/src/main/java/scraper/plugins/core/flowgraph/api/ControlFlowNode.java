package scraper.plugins.core.flowgraph.api;

import scraper.annotations.NotNull;
import scraper.api.Address;

/**
 */
public interface ControlFlowNode {
    /** @return address of this node */
    @NotNull
    Address getAddress();
    /** Type of node */
    @NotNull
    String getType();
}
