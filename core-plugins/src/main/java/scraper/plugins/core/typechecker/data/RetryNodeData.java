package scraper.plugins.core.typechecker.data;


import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.plugins.core.typechecker.TypeChecker;
import scraper.plugins.core.typechecker.TypeEnvironment;
import scraper.util.NodeUtil;

import java.util.List;
import java.util.Set;

@SuppressWarnings({"unused", "OptionalGetWithoutIsPresent"})
public final class RetryNodeData {

    @Version("0.1.0")
    public static void infoAfter(TypeChecker t, TypeEnvironment env, NodeContainer<?> node, ControlFlowGraph cfg, ScrapeInstance spec, List<NodeContainer<?>> visited) throws Exception {
        Address target = (Address) FlowUtil.getField("retryTarget", node.getC()).get();
        NodeContainer<? extends Node> nodeTarget = NodeUtil.getTarget(node.getAddress(), target, spec);

        TypeChecker newChecker = new TypeChecker(t);
        TypeEnvironment newEnvironment = env.copy();
        newChecker.propagate(nodeTarget, newEnvironment, spec, cfg, visited);

        env.merge(newEnvironment);
    }

}

