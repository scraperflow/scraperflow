package scraper.plugins.core.flowgraph.control;

import scraper.annotations.NotNull;
import scraper.api.node.Address;
import scraper.api.node.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.util.NodeUtil;

import java.util.List;

import static scraper.plugins.core.flowgraph.impl.ControlFlowEdgeImpl.edge;

public final class AbstractNodeControl {
    // default implementation (AbstractNode) of any node is only concerned with forward to next/goTo node, if enabled
    @Version("1.0.1") @NotNull
    public static List<ControlFlowEdge> getOutput(@SuppressWarnings("unused") List<ControlFlowEdge> ignore, Node node, ScrapeInstance spec) {
        Address origin = node.getAddress();
        Address goTo = node.getGoTo();

        Address nextTarget = NodeUtil.getNextNode(origin, goTo, spec.getGraphs());
        if(nextTarget == null) return List.of();

        Node nextNode = NodeUtil.getNode(nextTarget, spec.getGraphs(), spec.getImportedInstances());
        if(node.isForward()) {
            if(goTo != null)
                return List.of(edge(node.getAddress(), nextNode.getAddress(), "goTo"));
            else
                return List.of(edge(node.getAddress(), nextNode.getAddress(), "forward"));
        }

        return List.of();
    }
}
