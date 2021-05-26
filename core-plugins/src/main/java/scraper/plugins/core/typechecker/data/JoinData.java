package scraper.plugins.core.typechecker.data;


import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.api.template.T;
import scraper.core.AbstractStreamNode;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.plugins.core.typechecker.TypeChecker;
import scraper.plugins.core.typechecker.TypeEnvironment;
import scraper.util.NodeUtil;
import scraper.util.TemplateUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.System.Logger.Level.DEBUG;
import static scraper.plugins.core.typechecker.TypeChecker.getDefaultDataFlowOutput;
import static scraper.util.TemplateUtil.parseTemplate;

@SuppressWarnings({"unused", "OptionalGetWithoutIsPresent"})
public final class JoinData {
    private static final System.Logger log = System.getLogger("TypeChecker");

    @Version("0.1.0") @NotNull
    public static void infoAfter(TypeChecker checker, TypeEnvironment env, ControlFlowEdge incoming, NodeContainer<? extends Node> node, ControlFlowGraph cfg,
                                 ScrapeInstance spec, List<NodeContainer<?>> visited) throws Exception {
        T<Map<String, String>> mergeKeysT = (T<Map<String, String>>) FlowUtil.getField("keys", node.getC()).get();
        Map<String, String> targetToKeys = new FlowMapImpl().evalIdentity(mergeKeysT);

        Address target = (Address) FlowUtil.getField("joinTarget", node.getC()).get();

        NodeContainer<? extends Node> nodeTarget = NodeUtil.getTarget(node.getAddress(), target, spec);

        TypeEnvironment newEnvironment = env.copy();

        // propagate
        List<ControlFlowEdge> out = cfg.getOutgoingEdges(node.getAddress());
        ControlFlowEdge outEdge = out.stream()
                .filter(edge -> edge.getToAddress().equals(nodeTarget.getAddress()))
                .findFirst().get();

        // add list info
        targetToKeys.forEach((key, value) -> {
            T<String> fromFork = new T<>(){};
            fromFork.setTerm(parseTemplate(key, fromFork));

            T<?> fromForkToken = env.get(fromFork.getTerm());
            if (fromForkToken == null)
                throw new TemplateException("Join target does not have access to key at " + key);

            T<String> toJoin = new T<>() {};
            toJoin.setTerm(parseTemplate(value, toJoin));

            env.add(toJoin.getTerm(), TemplateUtil.listOf(fromForkToken));
        });
    }
}

