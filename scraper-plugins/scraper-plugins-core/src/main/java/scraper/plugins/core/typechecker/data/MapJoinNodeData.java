package scraper.plugins.core.typechecker.data;


import scraper.api.exceptions.TemplateException;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.api.template.T;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.plugins.core.typechecker.TypeChecker;
import scraper.util.NodeUtil;
import scraper.util.TemplateUtil;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import static scraper.util.TemplateUtil.parseTemplate;

@SuppressWarnings({"unused", "OptionalGetWithoutIsPresent", "unchecked"})
public final class MapJoinNodeData {

    @Version("0.1.0")
    public static void infoAfter(TypeChecker t, NodeContainer<?> node, ControlFlowGraph cfg, ScrapeInstance spec, Set<NodeContainer<?>> visited) throws Exception {
        T<Map<String, String>> mergeKeysT = (T<Map<String, String>>) FlowUtil.getField("keys", node.getC()).get();
        Map<String, String> targetToKeys = new FlowMapImpl().evalIdentity(mergeKeysT);

        Address target = (Address) FlowUtil.getField("mapTarget", node.getC()).get();
        String put = (String) FlowUtil.getField("putElement", node.getC()).get();

        NodeContainer<? extends Node> nodeTarget = NodeUtil.getTarget(node.getAddress(), target, spec);

        TypeChecker newChecker = new TypeChecker();
        newChecker.env = t.env.copy();
        newChecker.propagate(nodeTarget, newChecker.env, spec, cfg, visited);

        // add merged location infos
        targetToKeys.forEach((key, value) -> {
            T<String> fromFork = new T<>(){};
            fromFork.setTerm(parseTemplate(key, fromFork));

            Type fromForkToken = newChecker.env.get(fromFork.getTerm());
            if (fromForkToken == null)
                throw new TemplateException("Map join target does not produce key at " + key);

            T<String> toJoin = new T<>() {};
            toJoin.setTerm(parseTemplate(value, toJoin));

            t.env.add(toJoin.getTerm(), TemplateUtil.listOf(fromForkToken).get());
        });

    }

}

