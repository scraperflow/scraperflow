package scraper.plugins.core.flowgraph.control;

import scraper.annotations.NotNull;
import scraper.api.node.Address;
import scraper.api.node.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.Version;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static scraper.plugins.core.flowgraph.impl.ControlFlowEdgeImpl.edge;

public final class IfThenElseNodeControl {

    // if OR else target
    @Version("0.1.0") @NotNull
    public static List<ControlFlowEdge> getOutput(List<ControlFlowEdge> previous, Node node, ScrapeInstance spec) throws Exception {
        // 0.1.0 has trueTarget field and falseTarget field (Address)
        Address trueTarget = FlowUtil.getField("trueTarget", node);
        Address falseTarget = FlowUtil.getField("falseTarget", node);

        return Stream.concat(
                previous.stream(),
                Stream.of(
                        edge(node.getAddress(), trueTarget, "true"),
                        edge(node.getAddress(), falseTarget, "false")
                )
        ).collect(Collectors.toList());
    }
}
