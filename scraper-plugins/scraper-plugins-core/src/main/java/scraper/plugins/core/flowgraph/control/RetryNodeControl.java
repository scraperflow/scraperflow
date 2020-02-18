package scraper.plugins.core.flowgraph.control;


import scraper.annotations.NotNull;
import scraper.api.node.Address;
import scraper.api.node.NodeAddress;
import scraper.api.node.container.NodeContainer;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.util.NodeUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static scraper.plugins.core.flowgraph.impl.ControlFlowEdgeImpl.edge;

public final class RetryNodeControl {
    @Version("0.1.1") @NotNull
    public static List<ControlFlowEdge> getOutput(List<ControlFlowEdge> previous, NodeContainer node, ScrapeInstance spec) throws Exception {
        //noinspection OptionalGetWithoutIsPresent 0.1.0
        NodeAddress mapTarget = NodeUtil.getTarget(node.getAddress(), (Address) FlowUtil.getField("retryTarget", node.getC()).get(), spec).getAddress();

        return Stream.concat(
                previous.stream(),
                        Stream.of(edge(node.getAddress(), mapTarget, "retry", false, false))
        ).collect(Collectors.toList());
    }
}

