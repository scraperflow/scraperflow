package scraper.plugins.core.typechecker.data;


import scraper.api.node.container.NodeContainer;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.plugins.core.typechecker.TypeChecker;
import scraper.plugins.core.typechecker.TypeEnvironment;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"unused"})
public final class RedirectData {

    @Version("0.0.1")
    public static void infoAfter(TypeChecker t, TypeEnvironment env, ControlFlowEdge inc, NodeContainer<?> node, ControlFlowGraph cfg, ScrapeInstance spec, List<NodeContainer<?>> visited) throws Exception {
        List<ControlFlowEdge> edges = cfg.getOutgoingEdges(node.getAddress());

        {
            List<ControlFlowEdge> st = edges.stream()
                    .filter(e -> !e.getDisplayLabel().equalsIgnoreCase("forward"))
                    .collect(Collectors.toList());

            List<TypeEnvironment> envs = new LinkedList<>();

            st.forEach(socketEdge -> {
                TypeEnvironment newEnvironment = env.copy();

                var next = spec.getNode(socketEdge.getToAddress());
                t.propagate(socketEdge, next, newEnvironment, spec, cfg, new LinkedList<>(visited));

                envs.add(newEnvironment);
            });

            envs.forEach(env::merge);
        }
    }

}

