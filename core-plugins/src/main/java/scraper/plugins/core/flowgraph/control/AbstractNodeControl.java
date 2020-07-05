package scraper.plugins.core.flowgraph.control;

import scraper.annotations.NotNull;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.Version;

import java.util.List;
import java.util.Optional;

import static scraper.plugins.core.flowgraph.impl.ControlFlowEdgeImpl.edge;

@SuppressWarnings("unused") // reflection access
public final class AbstractNodeControl {
    // default implementation (AbstractNode) of any node is only concerned with forward to next/goTo node, if enabled
    @Version("1.0.1") @NotNull
    public static List<ControlFlowEdge> getOutput(List<ControlFlowEdge> ignore, NodeContainer<? extends Node> node, ScrapeInstance spec) {
        Optional<NodeContainer<? extends Node>> goTo = node.getGoTo();

        if(goTo.isEmpty()) return List.of();

        if(node.isForward()) return List.of(edge(node.getAddress(), goTo.get().getAddress(), "forward"));

        return List.of();
    }
}
