package scraper.plugins.core.flowgraph.api;


import scraper.api.node.Address;

import java.util.Map;

/**
 * @since 1.0.0
 */
public interface DataFlowGraph {
    Map<Address, DataFlowNode> getNodes();
    DataFlowNode getDataFlowFor(Address target);
}

