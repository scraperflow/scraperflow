package scraper.plugins.core.flowgraph.control;


import scraper.annotations.NotNull;
import scraper.api.node.Address;
import scraper.api.node.NodeAddress;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.T;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.util.NodeUtil;
import scraper.api.flow.impl.IdentityFlowMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static scraper.plugins.core.flowgraph.impl.ControlFlowEdgeImpl.edge;

@SuppressWarnings({"unchecked", "OptionalGetWithoutIsPresent"})
public final class SocketNodeControl {
    @Version("1.0.0") @NotNull
    public static List<ControlFlowEdge> getOutput(List<ControlFlowEdge> previous, NodeContainer<? extends Node> node, ScrapeInstance spec) throws Exception {
        Optional<Map<String, Address>> hosts = Optional.ofNullable(((T<Map<String, Address>>) FlowUtil.getField("hostMap", node.getC()).get()).getTerm().eval(new IdentityFlowMap()));
        Optional<Map<String, Address>> args = Optional.ofNullable(((T<Map<String, Address>>) FlowUtil.getField("args", node.getC()).get()).getTerm().eval(new IdentityFlowMap()));

        return Stream.concat(
                previous.stream(),
                Stream.concat(
                        hosts.orElseGet(Map::of).entrySet().stream().map(e -> of(e, node.getAddress(), spec)),
                        args.orElseGet(Map::of).entrySet().stream().map(e -> of(e, node.getAddress(), spec))
                )
        ).collect(Collectors.toList());
    }


    private static ControlFlowEdge of(Map.Entry<String, Address> e, NodeAddress from, ScrapeInstance spec) {
        NodeContainer<? extends Node> target = NodeUtil.getTarget(from, e.getValue(), spec);
        return edge(from, target.getAddress(), e.getKey());
    }
}

