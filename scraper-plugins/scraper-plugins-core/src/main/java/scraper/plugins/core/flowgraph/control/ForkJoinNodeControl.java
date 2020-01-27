package scraper.plugins.core.flowgraph.control;


import scraper.annotations.NotNull;
import scraper.api.node.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.util.NodeUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static scraper.plugins.core.flowgraph.impl.ControlFlowEdgeImpl.edge;

public final class ForkJoinNodeControl {
    @Version("0.1.0") @NotNull
    public static List<ControlFlowEdge> getOutput(List<ControlFlowEdge> previous, Node node, ScrapeInstance spec) throws Exception {
        // 0.1.0 has forkTargets
        List<String> forkTargets = FlowUtil.getField("forkTargets", node);

        return Stream.concat(
                previous.stream(),
                forkTargets.stream().map(label ->
                    edge(node.getAddress(), NodeUtil.addressOf(label), "forkJoin", false, true)
                )
        ).collect(Collectors.toList());
    }
}
