package scraper.plugins.core.typechecker.data;


import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.container.NodeContainer;
import scraper.api.specification.ScrapeInstance;
import scraper.api.template.T;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.plugins.core.typechecker.TypeChecker;
import scraper.plugins.core.typechecker.TypeEnvironment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "OptionalGetWithoutIsPresent", "unchecked"})
public final class LetInNodeData {

    @Version("0.0.1")
    public static void infoAfter(TypeChecker t, TypeEnvironment env, ControlFlowEdge inc, NodeContainer<?> node, ControlFlowGraph cfg, ScrapeInstance spec, List<NodeContainer<?>> visited) throws Exception {
        T<List<String>> targetsT = (T<List<String>>) FlowUtil.getField("keys", node.getC()).get();
        List<String> targets = new FlowMapImpl().eval(targetsT);

        var newInternal = env.templateToKnownTargets
                .entrySet().stream()
                .filter(e -> targets.contains(e.getKey().getRaw().toString()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        env.templateToKnownTargets = new HashMap<>(newInternal);
    }

}

