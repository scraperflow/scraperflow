package scraper.plugins.core.typechecker.data;


import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.plugins.core.typechecker.TypeChecker;
import scraper.plugins.core.typechecker.TypeEnvironment;
import scraper.util.NodeUtil;

import java.util.List;

@SuppressWarnings({"unused"})
public final class IfThenElseNodeData {

    @Version("0.1.0")
    public static void infoAfter(TypeChecker t, TypeEnvironment env, NodeContainer<?> node, ControlFlowGraph cfg, ScrapeInstance spec, List<NodeContainer<?>> visited) throws Exception {
        List<ControlFlowEdge> edges = cfg.getOutgoingEdges(node.getAddress());
        for (ControlFlowEdge edge : edges) {
            if ((edge.getDisplayLabel().contains("true")) || edge.getDisplayLabel().contains("false")) {
                edge.setPropagate(true);
            }
        }

    }

}

