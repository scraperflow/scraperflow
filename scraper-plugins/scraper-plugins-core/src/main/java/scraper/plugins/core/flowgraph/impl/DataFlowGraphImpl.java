package scraper.plugins.core.flowgraph.impl;


import scraper.api.node.Address;
import scraper.plugins.core.flowgraph.api.DataFlowGraph;
import scraper.plugins.core.flowgraph.api.DataFlowNode;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.0.0
 */
public class DataFlowGraphImpl implements DataFlowGraph {
    private final Map<Address, DataFlowNode> nodes = new HashMap<>();

    public void addNode(Address address, DataFlowNode node) { this.nodes.put(address, node); }

    @Override
    public Map<Address, DataFlowNode> getNodes() {
        return nodes;
    }

    @Override
    public DataFlowNode getDataFlowFor(Address target) {
        return nodes.get(target);
    }
}

