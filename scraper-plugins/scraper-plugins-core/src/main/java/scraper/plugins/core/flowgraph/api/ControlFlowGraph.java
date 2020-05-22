package scraper.plugins.core.flowgraph.api;


import scraper.annotations.NotNull;
import scraper.api.node.Address;

import java.util.Collection;
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

    /** @return Pre set of node */
    @NotNull Collection<ControlFlowNode> pre(Address node);

    /** @return Post set of node */
    @NotNull Collection<ControlFlowNode> post(Address node);
}

