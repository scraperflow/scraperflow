package scraper.plugins.core.flowgraph.control;


import scraper.annotations.NotNull;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.Address;
import scraper.api.node.NodeAddress;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.api.template.T;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.util.NodeUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static scraper.plugins.core.flowgraph.impl.ControlFlowEdgeImpl.edge;

@SuppressWarnings({"unchecked", "OptionalGetWithoutIsPresent"})
public final class RedirectNodeControl {

    @Version("0.0.1") @NotNull
    public static List<ControlFlowEdge> getOutput(List<ControlFlowEdge> previous, NodeContainer<? extends Node> node, ScrapeInstance spec) throws Exception {
        FlowMap o = new FlowMapImpl();
        Optional<Map<String, Address>> targets = o.evalMaybe(((T<Map<String, Address>>) FlowUtil.getField("redirectMap", node.getC()).get()));

        return Stream.concat(
                previous.stream(),
                targets.orElseGet(Map::of).entrySet().stream().map(e -> of(e, node.getAddress(), spec))
        ).collect(Collectors.toList());
    }


    private static ControlFlowEdge of(Map.Entry<String, Address> e, NodeAddress from, ScrapeInstance spec) {
        NodeContainer<? extends Node> target = NodeUtil.getTarget(from, e.getValue(), spec);
        return edge(from, target.getAddress(), e.getKey());
    }
}

