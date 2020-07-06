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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static scraper.plugins.core.flowgraph.impl.ControlFlowEdgeImpl.edge;

@SuppressWarnings("unused")
public final class IfThenElseNodeControl {

    // if OR else target
    @Version("0.1.0") @NotNull
    public static List<ControlFlowEdge> getOutput(List<ControlFlowEdge> previous, NodeContainer<? extends Node> node, ScrapeInstance spec) throws Exception {
        // 0.1.0 has trueTarget field and falseTarget field (Address)
        Optional<Address> trueTarget = FlowUtil.getField("trueTarget", node.getC());
        Optional<Address> falseTarget = FlowUtil.getField("falseTarget", node.getC());

        Stream<ControlFlowEdge> additionalOutput = Stream.concat(
                trueTarget.stream().map(a -> edge(node.getAddress(), NodeUtil.getTarget(node.getAddress(), a, spec).getAddress(), "true", true)),
                falseTarget.stream().map(a -> edge(node.getAddress(), NodeUtil.getTarget(node.getAddress(), a, spec).getAddress(), "false", true))
        );

        return Stream.concat(
                previous.stream(),
                additionalOutput
        ).collect(Collectors.toList());
    }

    @Version("0.1.0")
    public static void propagate(NodeContainer<?> node, ControlFlowGraph cfg, ScrapeInstance spec) throws Exception {
        List<ControlFlowEdge> edges = cfg.getOutgoingEdges(node.getAddress());


        LinkedList<ControlFlowEdge> newEdges = new LinkedList<>();

        if(edges.size() == 1) return; // nothing to reroute

        // at least one
        ControlFlowEdge forward = edges.get(0);

        ControlFlowEdge oneEdge = edges.get(1);

        {
            NodeAddress nextNode = oneEdge.getToAddress();
            while (spec.getNode(nextNode).getGoTo().isPresent()) {
                nextNode = spec.getNode(nextNode).getGoTo().get().getAddress();
            }

            newEdges.add(new ControlFlowEdgeImpl(nextNode, forward.getToAddress(), forward.getDisplayLabel(), forward.isMultiple(), forward.isDispatched()));
        }

        if(edges.size() == 2) return; // nothing to reroute
        ControlFlowEdge twoEdge = edges.get(2);

        {
            NodeAddress nextNode = twoEdge.getToAddress();
            while (spec.getNode(nextNode).getGoTo().isPresent()) {
                nextNode = spec.getNode(nextNode).getGoTo().get().getAddress();
            }

            newEdges.add(new ControlFlowEdgeImpl(nextNode, forward.getToAddress(), forward.getDisplayLabel(), forward.isMultiple(), forward.isDispatched()));
        }

        cfg.getEdges().remove(forward);
        cfg.getEdges().addAll(newEdges);
    }
}
