package scraper.plugins.core.typechecker.data;


import scraper.api.exceptions.TemplateException;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static scraper.util.TemplateUtil.parseTemplate;

@SuppressWarnings({"unused"})
public final class IfThenElseData {

    @Version("0.1.0")
    public static void infoAfter(TypeChecker t, TypeEnvironment env, ControlFlowEdge incoming, NodeContainer<?> node, ControlFlowGraph cfg, ScrapeInstance spec, List<NodeContainer<?>> visited) throws Exception {
        List<ControlFlowEdge> edges = cfg.getOutgoingEdges(node.getAddress());



        List<TypeEnvironment> envs = new ArrayList<>();
        edges.forEach(forkEdge -> {
            TypeEnvironment newEnvironment = env.copy();

            var forkTarget = spec.getNode(forkEdge.getToAddress());

            t.propagate(forkEdge, forkTarget, newEnvironment, spec, cfg, new LinkedList<>(visited));

            envs.add(newEnvironment);
        });

        assert envs.size() <= 2;

        if(envs.size() == 1) {
            env.merge(envs.get(0));
        } else {
            if(envs.get(0).greaterThan(envs.get(1))) {
                env.merge(envs.get(1));
            } else if(envs.get(1).greaterThan(envs.get(0))) {
                env.merge(envs.get(0));
            } else {
                env.merge(envs.get(0));
            }
        }
    }

}

