package scraper.plugins.core.flowgraph.control;

import scraper.annotations.NotNull;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.Version;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static scraper.plugins.core.flowgraph.impl.ControlFlowEdgeImpl.edge;

public final class IfThenElseNodeControl {

    // if OR else target
    @Version("0.1.0") @NotNull
    public static List<ControlFlowEdge> getOutput(List<ControlFlowEdge> previous, NodeContainer node, ScrapeInstance spec) throws Exception {
        // 0.1.0 has trueTarget field and falseTarget field (Address)
        Optional<Address> trueTarget = FlowUtil.getField("trueTarget", node.getC());
        Optional<Address> falseTarget = FlowUtil.getField("falseTarget", node.getC());

        Stream<ControlFlowEdge> additionalOutput = Stream.concat(
                trueTarget.stream().map(a -> edge(node.getAddress(), a, "true")),
                falseTarget.stream().map(a -> edge(node.getAddress(), a, "false"))
        );

        return Stream.concat(
                previous.stream(),
                additionalOutput
        ).collect(Collectors.toList());
    }
}
