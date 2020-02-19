package scraper.plugins.core.flowgraph.control;


import scraper.annotations.NotNull;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.reflect.T;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.util.NodeUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static scraper.plugins.core.flowgraph.impl.ControlFlowEdgeImpl.edge;

@SuppressWarnings({"OptionalGetWithoutIsPresent", "unchecked", "unused"}) // versioning, mandatory fields
public final class ForkJoinNodeControl {
    @Version("0.1.0") @NotNull
    public static List<ControlFlowEdge> getOutput(List<ControlFlowEdge> previous, NodeContainer<? extends Node> node, ScrapeInstance spec) throws Exception {
        T<List<Address>> tmp = (T<List<Address>>) FlowUtil.getField("forkTargets", node.getC()).get();
        List<Address> forkTargets = node.evalIdentity(tmp);

        return Stream.concat(
                previous.stream().filter(e -> !e.getDisplayLabel().equalsIgnoreCase("forward")),
                Stream.concat(previous.stream().filter(e -> e.getDisplayLabel().equalsIgnoreCase("forward")).map(e -> edge(e.getFromAddress(), e.getToAddress(), "join")),
                forkTargets.stream().map(label ->
                        {
                            NodeContainer<? extends Node> forkTarget = NodeUtil.getTarget(node.getAddress(), label, spec);
                            return edge(node.getAddress(), forkTarget.getAddress(), "fork", false, false);
                        }
                ))
        ).collect(Collectors.toList());
    }
}
