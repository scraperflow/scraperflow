package scraper.plugins.core.flowgraph.control;


import scraper.annotations.NotNull;
import scraper.api.flow.impl.IdentityFlowMap;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.reflect.T;
import scraper.api.specification.ScrapeInstance;
import scraper.core.Template;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.Version;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static scraper.plugins.core.flowgraph.impl.ControlFlowEdgeImpl.edge;

public final class ForkJoinNodeControl {
    @Version("0.1.0") @NotNull
    public static List<ControlFlowEdge> getOutput(List<ControlFlowEdge> previous, NodeContainer<? extends Node> node, ScrapeInstance spec) throws Exception {
        //noinspection unchecked, OptionalGetWithoutIsPresent 0.1.0 has forkTargets, mandatory
        List<Address> forkTargets = Template.eval((T<List<Address>>) FlowUtil.getField("forkTargets", node.getC()).get(), new IdentityFlowMap());

        return Stream.concat(
                previous.stream(),
                forkTargets.stream().map(label ->
                    edge(node.getAddress(), label, "fork", false, false)
                )
        ).collect(Collectors.toList());
    }
}
