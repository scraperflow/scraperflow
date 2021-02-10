package scraper.plugins.core.typechecker.data;


import scraper.api.exceptions.TemplateException;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.api.template.L;
import scraper.api.template.T;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.plugins.core.typechecker.TypeChecker;
import scraper.plugins.core.typechecker.TypeEnvironment;
import scraper.util.NodeUtil;
import scraper.util.TemplateUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.DEBUG;
import static scraper.util.TemplateUtil.parseTemplate;

@SuppressWarnings({"unused", "OptionalGetWithoutIsPresent", "unchecked"})
public final class ApplyData {

    private static final System.Logger log = System.getLogger("TypeChecker");

    @Version("0.1.0")
    public static void infoAfter(TypeChecker t, TypeEnvironment env, ControlFlowEdge inc, NodeContainer<?> node, ControlFlowGraph cfg, ScrapeInstance spec, List<NodeContainer<?>> visited) throws Exception {
        T<List<String>> supplyT = (T<List<String>>) FlowUtil.getField("supply", node.getC()).get();
        List<String> supply = new FlowMapImpl().evalIdentity(supplyT);
        
        T<Map<String, String>> mergeKeysT = (T<Map<String, String>>) FlowUtil.getField("keys", node.getC()).get();
        Map<String, String> targetToKeys = new FlowMapImpl().evalIdentity(mergeKeysT);

        Address target = (Address) FlowUtil.getField("applyTarget", node.getC()).get();

        
        NodeContainer<? extends Node> nodeTarget = NodeUtil.getTarget(node.getAddress(), target, spec);
        TypeEnvironment newEnvironment = new TypeEnvironment();

        var newInternal = env.templateToKnownTargets
                .entrySet().stream()
                .filter(e -> supply.contains(e.getKey().getRaw().toString()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        newEnvironment.templateToKnownTargets = new HashMap<>(newInternal);


        // propagate
        List<ControlFlowEdge> out = cfg.getOutgoingEdges(node.getAddress());
        ControlFlowEdge outEdge = out.stream()
                .filter(edge -> edge.getToAddress().equals(nodeTarget.getAddress()))
                .findFirst().get();

        t.propagate(outEdge, nodeTarget, newEnvironment, spec, cfg, new LinkedList<>(visited));

        // add apply location infos
        targetToKeys.forEach((key, value) -> {
            T<String> fromFork = new T<>(){};
            fromFork.setTerm(parseTemplate(key, fromFork));

             T<?> fromForkToken = newEnvironment.get(fromFork.getTerm());
            if (fromForkToken == null)
                throw new TemplateException("Apply target does not produce key at " + key);

            T<String> toJoin = new T<>() {};
            toJoin.setTerm(parseTemplate(value, toJoin));

            log.log(DEBUG, "<{0}> {1} :: {2}", node.getAddress(), toJoin.getTerm(), fromForkToken.get().getTypeName());
            env.add(toJoin.getTerm(), fromForkToken);
        });
    }

}

