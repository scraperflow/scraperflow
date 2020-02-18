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

public final class MapJoinNodeControl {
    @Version("0.1.0") @NotNull
    public static List<ControlFlowEdge> getOutput(List<ControlFlowEdge> previous, NodeContainer<? extends Node> node, ScrapeInstance spec) throws Exception {
        //noinspection OptionalGetWithoutIsPresent 0.1.0 has mapTarget
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
}
