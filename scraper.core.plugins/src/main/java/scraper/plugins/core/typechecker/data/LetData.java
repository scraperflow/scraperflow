package scraper.plugins.core.typechecker.data;


import scraper.api.NodeContainer;
import scraper.api.ScrapeInstance;
import scraper.api.L;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.plugins.core.typechecker.TypeChecker;
import scraper.plugins.core.typechecker.TypeEnvironment;

import java.util.List;

@SuppressWarnings({"unused", "OptionalGetWithoutIsPresent", "unchecked"})
public final class LetData {

    @Version("0.0.1")
    public static void infoBefore(TypeChecker t, TypeEnvironment env, ControlFlowEdge inc, NodeContainer<?> node, ControlFlowGraph cfg, ScrapeInstance spec, List<NodeContainer<?>> visited) throws Exception {
        L<?> shadow = (L<?>) FlowUtil.getField("put", node.getC()).get();
        env.remove(shadow.getLocation());
    }

}

