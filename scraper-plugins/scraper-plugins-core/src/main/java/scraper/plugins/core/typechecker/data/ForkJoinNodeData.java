package scraper.plugins.core.typechecker.data;


import org.slf4j.Logger;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.impl.FlowMapImpl;
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
import scraper.util.NodeUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static scraper.util.TemplateUtil.parseTemplate;

@SuppressWarnings({"unused", "OptionalGetWithoutIsPresent", "unchecked"})
public final class ForkJoinNodeData {


    @Version("0.1.0")
    public static void infoAfter(TypeChecker t, NodeContainer<?> node, ControlFlowGraph cfg, ScrapeInstance spec, Set<NodeContainer<?>> visited) throws Exception {
        T<Map<String, Map<String, String>>> mergeKeys = (T<Map<String, Map<String, String>>>) FlowUtil.getField("targetToKeys", node.getC()).get();
        Map<String, Map<String, String>> targetToKeys = new FlowMapImpl().evalIdentity(mergeKeys);

        // assume fork ... fork join edge list
        List<ControlFlowEdge> edges = cfg.getOutgoingEdges(node.getAddress());
        edges.stream()
                .filter(e -> e.getDisplayLabel().contains("fork"))
                .collect(Collectors.toList())
                .forEach(forkEdge -> {
                    TypeChecker newChecker = new TypeChecker();
                    newChecker.env = t.env.copy();

                    var forkTarget = spec.getNode(forkEdge.getToAddress());

                    newChecker.propagate(forkTarget, newChecker.env, spec, cfg, visited);

                    Map<String, String> keyMergeSet = null;
                    for (String s : targetToKeys.keySet()) {
                        Address address = NodeUtil.addressOf(s);
                        NodeContainer<? extends Node> test = NodeUtil.getTarget(node.getAddress(), address, spec);
                        if (forkEdge.getToAddress().equals(test.getAddress())) {
                            keyMergeSet = targetToKeys.get(s);
                        }
                    }

                    // add merged location infos
                    keyMergeSet.entrySet().forEach(e -> {
                        T<String> fromFork = new T<>() {
                        };
                        fromFork.setTerm(parseTemplate(e.getKey(), fromFork));

                        T<?> fromForkToken = newChecker.env.get(fromFork.getTerm());
                        if (fromForkToken == null)
                            throw new TemplateException("Fork join target does not produce key at " + e.getKey());

                        T<String> toJoin = new T<>() {
                        };
                        toJoin.setTerm(parseTemplate(e.getValue(), toJoin));
                        if (t.env.get(toJoin.getTerm()) != null)
                            throw new TemplateException("Fork joining the key '" + toJoin.getTerm()
                                    + "' with another type. " +
                                    "Choose another location to merge");
                        t.env.add(toJoin.getTerm(), fromForkToken);
                    });

                });
    }

}

