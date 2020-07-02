package scraper.plugins.core.flowgraph.control;


import scraper.annotations.NotNull;
import scraper.api.node.Address;
import scraper.api.node.NodeAddress;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.plugins.core.flowgraph.impl.ControlFlowEdgeImpl;
import scraper.util.NodeUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static scraper.plugins.core.flowgraph.impl.ControlFlowEdgeImpl.edge;

@SuppressWarnings({"unused", "OptionalGetWithoutIsPresent"})
public final class RetryNodeControl {
    @Version("0.1.1") @NotNull
    public static List<ControlFlowEdge> getOutput(List<ControlFlowEdge> previous, NodeContainer<? extends Node> node, ScrapeInstance spec) throws Exception {
        NodeAddress mapTarget = NodeUtil.getTarget(node.getAddress(), (Address) FlowUtil.getField("retryTarget", node.getC()).get(), spec).getAddress();

        List<ControlFlowEdge> out = Stream.concat(
                previous.stream(),
                Stream.of(edge(node.getAddress(), mapTarget, "retry", false, false))
        ).collect(Collectors.toList());

        return out;
    }

    @Version("0.1.0")
    public static void propagate(NodeContainer<?> node, ControlFlowGraph cfg, ScrapeInstance spec) throws Exception {
        List<ControlFlowEdge> edges = cfg.getOutgoingEdges(node.getAddress());

        if(edges.size() == 1) return; // nothing to reroute

        // at least one
        ControlFlowEdge forward = edges.get(0);
        ControlFlowEdge oneEdge = edges.get(1);

        {
            NodeAddress nextNode = oneEdge.getToAddress();
            while (spec.getNode(nextNode).getGoTo().isPresent()) {
                nextNode = spec.getNode(nextNode).getGoTo().get().getAddress();
            }

            forward.setFromAddress(nextNode);
        }
    }
}

