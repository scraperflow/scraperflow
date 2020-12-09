package scraper.plugins.core.typechecker.data;


import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.api.template.T;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.plugins.core.typechecker.TypeChecker;
import scraper.plugins.core.typechecker.TypeEnvironment;
import scraper.util.NodeUtil;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings({"unused"})
public final class DistributedRequestNodeData {

    @Version("0.1.0")
    public static void infoAfter(TypeChecker t, TypeEnvironment env, ControlFlowEdge incoming, NodeContainer<?> node, ControlFlowGraph cfg, ScrapeInstance spec, List<NodeContainer<?>> visited) throws Exception {
        Address target = (Address) FlowUtil.getField("distTarget", node.getC()).get();

        NodeContainer<? extends Node> nodeTarget = NodeUtil.getTarget(node.getAddress(), target, spec);
        List<ControlFlowEdge> out = cfg.getOutgoingEdges(node.getAddress());
        ControlFlowEdge outEdge = out.stream()
                .filter(edge -> edge.getToAddress().equals(nodeTarget.getAddress()))
                .findFirst().get();

        TypeEnvironment newEnvironment = env.copy();
        t.propagate(outEdge, nodeTarget, newEnvironment, spec, cfg, new LinkedList<>(visited));

        env.merge(newEnvironment);
    }

}

