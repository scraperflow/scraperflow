package scraper.plugins.core.typechecker.data;


import org.slf4j.Logger;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.api.template.L;
import scraper.api.template.T;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.plugins.core.typechecker.TypeChecker;
import scraper.plugins.core.typechecker.TypeEnvironment;
import scraper.util.NodeUtil;
import scraper.util.TemplateUtil;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import static scraper.util.TemplateUtil.parseTemplate;

@SuppressWarnings({"unused", "OptionalGetWithoutIsPresent", "unchecked"})
public final class MapJoinNodeData {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger("TypeChecker");

    @Version("0.1.0")
    public static void infoAfter(TypeChecker t, TypeEnvironment env, NodeContainer<?> node, ControlFlowGraph cfg, ScrapeInstance spec, Set<NodeContainer<?>> visited) throws Exception {
        T<Map<String, String>> mergeKeysT = (T<Map<String, String>>) FlowUtil.getField("keys", node.getC()).get();
        Map<String, String> targetToKeys = new FlowMapImpl().evalIdentity(mergeKeysT);

        Address target = (Address) FlowUtil.getField("mapTarget", node.getC()).get();
        L<?> put = (L<?>) FlowUtil.getField("putElement", node.getC()).get();

        NodeContainer<? extends Node> nodeTarget = NodeUtil.getTarget(node.getAddress(), target, spec);

        TypeChecker newChecker = new TypeChecker(t);
        TypeEnvironment newEnvironment = env.copy();

        // add putElement into new environment and propagate
        newChecker.add(newEnvironment, put.getLocation(), put.getTarget());
        newChecker.propagate(nodeTarget, newEnvironment, spec, cfg, visited);

        // add merged location infos
        targetToKeys.forEach((key, value) -> {
            T<String> fromFork = new T<>(){};
            fromFork.setTerm(parseTemplate(key, fromFork));

             T<?> fromForkToken = newEnvironment.get(fromFork.getTerm());
            if (fromForkToken == null)
                throw new TemplateException("Map join target does not produce key at " + key);

            T<String> toJoin = new T<>() {};
            toJoin.setTerm(parseTemplate(value, toJoin));

            log.info("<{}> {} :: {}", node.getAddress(), toJoin.getTerm(), "java.lang.List<"+fromForkToken.get().getTypeName()+">");
            env.add(toJoin.getTerm(), TemplateUtil.listOf(fromForkToken.get()));
        });
    }

}

