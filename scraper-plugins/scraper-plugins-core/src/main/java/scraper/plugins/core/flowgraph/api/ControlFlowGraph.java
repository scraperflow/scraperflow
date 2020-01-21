package scraper.plugins.core.flowgraph.api;


import scraper.api.node.Address;

import java.util.List;
import java.util.Map;

/**
 * @since 1.0.0
 */
public interface ControlFlowGraph {
    Map<Address, ControlFlowNode> getNodes();
    List<ControlFlowEdge> getEdges();

    List<ControlFlowEdge> getOutgoingEdges(Address node);
    List<ControlFlowEdge> getIncomingEdges(Address node);
}

