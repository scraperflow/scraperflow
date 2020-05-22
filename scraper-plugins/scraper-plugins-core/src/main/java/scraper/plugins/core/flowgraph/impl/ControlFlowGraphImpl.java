package scraper.plugins.core.flowgraph.impl;


import scraper.api.node.Address;
import scraper.api.node.NodeAddress;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.ControlFlowNode;

import java.util.*;
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

    @Override
    public Collection<ControlFlowNode> pre(Address node) {
        return visitPre(node, new HashSet<>());
    }

    private Collection<ControlFlowNode> visitPre(Address node, Set<Address> visited) {
        if(visited.contains(node)) return Set.of();
        visited.add(node);

        List<ControlFlowNode> pre = new LinkedList<>();

        List<ControlFlowEdge> incomingEdges = getIncomingEdges(node);
        for (ControlFlowEdge edge : incomingEdges) {
            NodeAddress preAddress = edge.getFromAddress();
            pre.add(nodes.get(preAddress));
            pre.addAll(visitPre(preAddress, visited));
        }

        return pre;
    }

    @Override
    public Collection<ControlFlowNode> post(Address node) {
        return Set.of();
    }

    public void addNode(Address address, ControlFlowNode node) { this.nodes.put(address, node); }
    public void addEdge(ControlFlowEdge edge) { this.edges.add(edge); }
}

