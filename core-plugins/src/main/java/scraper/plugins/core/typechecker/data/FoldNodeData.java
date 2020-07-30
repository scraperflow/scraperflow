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
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.plugins.core.typechecker.TypeChecker;
import scraper.plugins.core.typechecker.TypeEnvironment;
import scraper.util.NodeUtil;
import scraper.util.TemplateUtil;

import java.util.List;
import java.util.Map;

import static java.lang.System.Logger.Level.*;
import static scraper.util.TemplateUtil.parseTemplate;

@SuppressWarnings({"unused", "OptionalGetWithoutIsPresent", "unchecked"})
public final class FoldNodeData {

    private static final System.Logger log = System.getLogger("TypeChecker");

    @Version("0.1.0")
    public static void infoAfter(TypeChecker t, TypeEnvironment env, NodeContainer<?> node, ControlFlowGraph cfg, ScrapeInstance spec, List<NodeContainer<?>> visited) throws Exception {
//        T<Map<String, String>> mergeKeysT = (T<Map<String, String>>) FlowUtil.getField("keys", node.getC()).get();
//        Map<String, String> targetToKeys = new FlowMapImpl().evalIdentity(mergeKeysT);

        Address target = (Address) FlowUtil.getField("foldTarget", node.getC()).get();
        L<?> putE = (L<?>) FlowUtil.getField("putElement", node.getC()).get();
        L<?> putAcc = (L<?>) FlowUtil.getField("putAccumulate", node.getC()).get();
        L<?> retrieveAcc = (L<?>) FlowUtil.getField("retrieveAccumulate", node.getC()).get();
        L<?> putRes = (L<?>) FlowUtil.getField("result", node.getC()).get();


        NodeContainer<? extends Node> nodeTarget = NodeUtil.getTarget(node.getAddress(), target, spec);

        {
            TypeChecker newChecker = new TypeChecker(t);
            TypeEnvironment newEnvironment = env.copy();

            // remove result location and retrieve location
            newEnvironment.remove(putRes.getLocation());
            newEnvironment.remove(retrieveAcc.getLocation());

            // propagate and type check
            newChecker.propagate(nodeTarget, newEnvironment, spec, cfg, visited);

            // check if retrieve is set
            if (newEnvironment.get(retrieveAcc.getLocation()) == null)
                throw new TemplateException("Accumulator cannot be retrieved, not set " + retrieveAcc);

        }

        // remove retrieved location, put location, put element from root
        env.remove(putE.getLocation());
        env.remove(putAcc.getLocation());
        env.remove(retrieveAcc.getLocation());
    }

}

