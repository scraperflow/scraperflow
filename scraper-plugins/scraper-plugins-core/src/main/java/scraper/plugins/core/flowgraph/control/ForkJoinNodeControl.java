package scraper.plugins.core.flowgraph.control;


import scraper.annotations.NotNull;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.Version;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static scraper.plugins.core.flowgraph.impl.ControlFlowEdgeImpl.edge;

public final class ForkJoinNodeControl {
    @Version("0.1.0") @NotNull
    public static List<ControlFlowEdge> getOutput(List<ControlFlowEdge> previous, NodeContainer node, ScrapeInstance spec) throws Exception {
        //noinspection unchecked, OptionalGetWithoutIsPresent 0.1.0 has forkTargets, mandatory
        List<Address> forkTargets = (List<Address>) FlowUtil.getField("forkTargets", node.getC()).get();

        return Stream.concat(
                previous.stream(),
                forkTargets.stream().map(label ->
                    edge(node.getAddress(), label, "forkJoin", false, true)
                )
        ).collect(Collectors.toList());
    }
}
