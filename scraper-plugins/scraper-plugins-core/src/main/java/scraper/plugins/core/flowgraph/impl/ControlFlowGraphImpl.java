package scraper.plugins.core.flowgraph.impl;


import scraper.api.node.Address;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.ControlFlowNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @since 1.0.0
 */
public class ControlFlowGraphImpl implements ControlFlowGraph {
    private Map<Address, ControlFlowNode> nodes = new HashMap<>();
    private List<ControlFlowEdge> edges = new LinkedList<>();


    @Override
    public Map<Address, ControlFlowNode> getNodes() {
        return nodes;
    }

    @Override
    public List<ControlFlowEdge> getEdges() {
        return edges;
    }

    @Override
    public List<ControlFlowEdge> getOutgoingEdges(Address node) {
        return edges.stream()
                .filter(edge -> edge.getFromAddress().equals(node))
                .collect(Collectors.toList());
    }

    @Override
    public List<ControlFlowEdge> getIncomingEdges(Address node) {
        return edges.stream()
                .filter(edge -> edge.getToAddress().equals(node))
                .collect(Collectors.toList());
    }

    public void addNode(Address address, ControlFlowNode node) { this.nodes.put(address, node); }
    public void addEdge(ControlFlowEdge edge) { this.edges.add(edge); }
}

