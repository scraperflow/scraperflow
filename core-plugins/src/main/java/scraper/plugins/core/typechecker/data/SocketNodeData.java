package scraper.plugins.core.typechecker.data;


import scraper.api.exceptions.TemplateException;
import scraper.api.node.container.NodeContainer;
import scraper.api.specification.ScrapeInstance;
import scraper.api.template.T;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.plugins.core.typechecker.TypeChecker;
import scraper.plugins.core.typechecker.TypeEnvironment;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "OptionalGetWithoutIsPresent", "unchecked"})
public final class SocketNodeData {

    @Version("0.1.0")
    public static void infoBefore(TypeChecker t, TypeEnvironment env, NodeContainer<?> node, ControlFlowGraph cfg, ScrapeInstance spec, List<NodeContainer<?>> visited) throws Exception {
        t.ignoreField("expected");

    }

    @Version("0.1.0")
    public static void infoAfter(TypeChecker t, TypeEnvironment env, NodeContainer<?> node, ControlFlowGraph cfg, ScrapeInstance spec, List<NodeContainer<?>> visited) throws Exception {
        t.unignoreField("expected");

        T<String> expected = (T<String>) FlowUtil.getField("expected", node.getC()).get();

        List<ControlFlowEdge> edges = cfg.getOutgoingEdges(node.getAddress());

        {
            List<ControlFlowEdge> st = edges.stream()
                    .filter(e -> !e.getDisplayLabel().equalsIgnoreCase("forward"))
                    .collect(Collectors.toList());

            if(st.isEmpty()) throw new TemplateException("Template 'expected' can never be written, no outgoing edges");
            st
                    .forEach(socketEdge -> {
                        TypeChecker newChecker = new TypeChecker(t);
                        TypeEnvironment newEnvironment = env.copy();

                        var socketTarget = spec.getNode(socketEdge.getToAddress());
                        newChecker.propagate(socketTarget, newEnvironment, spec, cfg, visited);

                        // Type expected for every special edge
                        newChecker.typeTemplate(newEnvironment, expected);
                    });
        }
    }

}

