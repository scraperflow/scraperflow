package scraper.plugins.core.flowgraph.control;


import scraper.annotations.NotNull;
import scraper.api.flow.impl.IdentityFlowMap;
import scraper.api.node.Address;
import scraper.api.node.NodeAddress;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.api.template.T;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.util.NodeUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static scraper.plugins.core.flowgraph.impl.ControlFlowEdgeImpl.edge;

@SuppressWarnings({"unused", "unchecked", "OptionalGetWithoutIsPresent"})
public final class PipeNodeControl {
    @Version("1.0.0") @NotNull
    public static List<ControlFlowEdge> getOutput(List<ControlFlowEdge> previous, NodeContainer<? extends Node> node, ScrapeInstance spec) throws Exception {
        List<Address> pipeTargets = ((T<List<Address>>) FlowUtil.getField("pipeTargets", node.getC()).get()).getTerm().eval(new IdentityFlowMap());

        return Stream.concat(
                IntStream.rangeClosed(0, pipeTargets.size()-1).mapToObj(i ->
                        {
                            NodeContainer<? extends Node> pipeTarget = NodeUtil.getTarget(node.getAddress(), pipeTargets.get(i), spec);
                            return edge(node.getAddress(), pipeTarget.getAddress(), "pipe@"+i);
                        }
                ),
                previous.stream()
        ).collect(Collectors.toList());
    }

    @Version("1.0.0")
    public static void propagate(NodeContainer<?> node, ControlFlowGraph cfg, ScrapeInstance spec) throws Exception {
        // assume pipe@0 ... pipe@i forward edge list sorted
        List<ControlFlowEdge> edges = cfg.getOutgoingEdges(node.getAddress());

        if(edges.size() == 1) return; // nothing to reroute

        NodeAddress fromTarget = null;

        boolean first = true;
        NodeAddress nextNode;

        for (ControlFlowEdge edge : edges){
            if(!first) { edge.setFromAddress(fromTarget); }
            first = false;

            nextNode = edge.getToAddress();
            while(spec.getNode(nextNode).getGoTo().isPresent()) {
                nextNode = spec.getNode(nextNode).getGoTo().get().getAddress();
            }

            fromTarget = nextNode;
        }

    }
}

