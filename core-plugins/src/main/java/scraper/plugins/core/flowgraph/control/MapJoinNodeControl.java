package scraper.plugins.core.flowgraph.control;


import scraper.annotations.NotNull;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.util.NodeUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static scraper.plugins.core.flowgraph.impl.ControlFlowEdgeImpl.edge;

@SuppressWarnings({"unused", "OptionalGetWithoutIsPresent"})
public final class MapJoinNodeControl {
    @Version("0.1.0") @NotNull
    public static List<ControlFlowEdge> getOutput(List<ControlFlowEdge> previous, NodeContainer<? extends Node> node, ScrapeInstance spec) throws Exception {
        Address mapTarget = (Address) FlowUtil.getField("mapTarget", node.getC()).get();
        NodeContainer<? extends Node> map = NodeUtil.getTarget(node.getAddress(), mapTarget, spec);

        return Stream.concat(
                previous.stream().filter(e -> !e.getDisplayLabel().equalsIgnoreCase("forward")),
                Stream.concat(
                        previous.stream().filter(e -> e.getDisplayLabel().equalsIgnoreCase("forward")).map(e -> edge(e.getFromAddress(), e.getToAddress(), "join")),
                        Stream.of(edge(node.getAddress(), map.getAddress(), "map", true, false))
                )
        ).collect(Collectors.toList());
    }


//    @Version("0.1.0")
//    public static void propagate(NodeContainer<?> node, ControlFlowGraph cfg, ScrapeInstance spec) throws Exception {
//        // assume fork ... fork join edge list sorted
//        List<ControlFlowEdge> edges = cfg.getOutgoingEdges(node.getAddress());
//
//        if(edges.size() == 1) return; // nothing to reroute
//
//
//        ControlFlowEdge joinEdge = edges.get(0);
//        cfg.getEdges().remove(joinEdge);
//
//        // skip join
//        List<ControlFlowEdge> newEdges = edges.stream().skip(1).map(edge -> {
//            NodeAddress nextNode = edge.getToAddress();
//            while (spec.getNode(nextNode).getGoTo().isPresent()) {
//                nextNode = spec.getNode(nextNode).getGoTo().get().getAddress();
//            }
//
//            return new ControlFlowEdgeImpl(nextNode, joinEdge.getToAddress(), joinEdge.getDisplayLabel(), joinEdge.isMultiple(), true);
//        }).collect(Collectors.toList());
//
//        cfg.getEdges().addAll(newEdges);
//    }
}
