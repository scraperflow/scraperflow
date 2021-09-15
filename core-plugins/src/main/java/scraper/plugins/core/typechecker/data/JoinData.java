package scraper.plugins.core.typechecker.data;


import scraper.annotations.NotNull;
import scraper.api.TemplateException;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.Address;
import scraper.api.NodeContainer;
import scraper.api.Node;
import scraper.api.ScrapeInstance;
import scraper.api.T;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.plugins.core.typechecker.TypeChecker;
import scraper.plugins.core.typechecker.TypeEnvironment;
import scraper.util.NodeUtil;
import scraper.util.TemplateUtil;

import java.util.List;
import java.util.Map;

import static scraper.util.TemplateUtil.parseTemplate;

@SuppressWarnings({"unused", "OptionalGetWithoutIsPresent"})
public final class JoinData {
    private static final System.Logger log = System.getLogger("TypeChecker");

    @SuppressWarnings("unchecked")
    @Version("0.1.0") @NotNull
    public static void infoAfter(TypeChecker checker, TypeEnvironment env, ControlFlowEdge incoming, NodeContainer<? extends Node> node, ControlFlowGraph cfg,
                                 ScrapeInstance spec, List<NodeContainer<?>> visited) throws Exception {
        T<Map<String, String>> mergeKeysT = (T<Map<String, String>>) FlowUtil.getField("keys", node.getC()).get();
        Map<String, String> targetToKeys = new FlowMapImpl().evalIdentity(mergeKeysT);

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

