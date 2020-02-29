package scraper.plugins.core.analysis;

import org.slf4j.Logger;
import scraper.annotations.node.FlowKey;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.api.template.T;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.util.NodeUtil;
import scraper.utils.ClassUtil;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StaticCheckUtil {
    private static final Logger l = org.slf4j.LoggerFactory.getLogger("StaticAnalysis");

    // term -> type mapping
    private static final Map<String, T<?>> S = new ConcurrentHashMap<>();

    public static void check(ScrapeInstance job, ControlFlowGraph cfg) throws ValidationException {
        l.info("Statically checking {}", job.getName());
        if (job.getEntry().isEmpty()) return;

        var startNode = job.getEntry().get();

        // start node should not have incoming edges
        // this restriction could be removed in the future
        if (!cfg.getIncomingEdges(startNode.getAddress()).isEmpty()) {
            throw new ValidationException("Start node for job "+job.getName()+" has incoming control flow edges");
        }

        // FIXME do not depend on impl in scraper.core
        pre(startNode, new FlowMapImpl(), job, cfg);
    }

    private static void pre(NodeContainer<? extends Node> n, FlowMap o, ScrapeInstance job, ControlFlowGraph cfg) throws ValidationException {
//        for (ControlFlowEdge i : cfg.getIncomingEdges(n.getAddress())) {
//            var incomingNode = job.getNode(i.getFromAddress());
//            throw new IllegalStateException("Not implemented yet");
//        }

        Map<String, T<?>> inputTokens = getDefaultDataFlowInput(n);
        for (String k : inputTokens.keySet()) {
            l.info(k + " -> " + inputTokens.get(k).get());
//            T<?> toCheck = inputTokens.get(k);
//
//            shouldEval(toCheck, o);

        }

//        l.info("MOD success for {}", n);
//
//        l.info("CONTINUE {}", n);
    }

    public static void shouldEval(T<?> toCheck, FlowMap o) throws ValidationException {
        o.eval(toCheck);
    }

//    @NotNull
//    public static DataFlowGraph generateDataFlowGraph(ScrapeInstance instance) {
//        DataFlowGraphImpl dfg = new DataFlowGraphImpl();
//
//        instance.getGraphs().forEach(((graphAddress, nodes) ->
//                nodes.forEach(node -> handleNodeDfg(dfg, instance, node))
//        ));
//
//        return dfg;
//    }

//    private static void handleNodeDfg(DataFlowGraphImpl dfg, ScrapeInstance instance, NodeContainer<? extends Node> node) {
//        DataFlowNodeImpl cfnode = new DataFlowNodeImpl(node.getAddress());
//        dfg.addNode(node.getAddress(), cfnode);
//
//        List<Class> classesToCheck = getReverseOrderHierarchy(node);
//
//        // default input
//        Map<String, String> defaultsInput = getDefaultDataFlowInput(node);
//        Map<String, String> input = new HashMap<>(defaultsInput);
//
//        // default output
//        Map<String, String> defaults = getDefaultDataFlowOutput(node);
//        Map<String, String> output = new HashMap<>(defaults);
//
//        for (Class nodeClass : classesToCheck) {
//            String controlClass = "scraper.plugins.core.flowgraph.data."+nodeClass.getSimpleName()+"Data";
//            try {
//                Class<?> control = Class.forName(controlClass);
//                Method method = control.getDeclaredMethod("getOutput", Map.class, NodeContainer.class, ScrapeInstance.class);
//                //noinspection unchecked
//                output = (Map<String, String>) method.invoke(null, output, node, instance);
//
//                Method method2 = control.getDeclaredMethod("getInput", Map.class, NodeContainer.class, ScrapeInstance.class);
//                //noinspection unchecked
//                input = (Map<String, String>) method2.invoke(null, input, node, instance);
//            } catch (Exception ignored) {
//                System.out.println("[Skip] Could not find data for " + nodeClass+": " + controlClass);
//            }
//        }
//
//        output.forEach(cfnode::addProduce);
//        input.forEach(cfnode::addConsume);
//
//        dfg.addNode(node.getAddress(), cfnode);
//    }

    private static Map<String, T<?>> getDefaultDataFlowOutput(NodeContainer<? extends Node> node) {
        l.info("Get OUT of {}", node.getC().getClass().getSimpleName());
        List<Field> outputData = ClassUtil.getAllFields(new LinkedList<>(), node.getC().getClass()).stream()
                // only templates
                .filter(field -> field.getType().isAssignableFrom(T.class))
                // only annotated by flow keys
                .filter(field -> field.getAnnotation(FlowKey.class) != null)
//                // only output
//                .filter(field -> field.getAnnotation(FlowKey.class).output())
                .collect(Collectors.toList());
        return NodeUtil.extractMapFromFields(outputData, node);
    }

    private static Map<String, T<?>> getDefaultDataFlowInput(NodeContainer<? extends Node> node) {
        l.info("Get IN of {}", node.getC().getClass().getSimpleName());
        List<Field> inputData = ClassUtil.getAllFields(new LinkedList<>(), node.getC().getClass()).stream()
                // only templates
                .filter(field -> field.getType().isAssignableFrom(T.class))
                // only annotated by flow keys
                .filter(field -> field.getAnnotation(FlowKey.class) != null)
//                // only input
//                .filter(field -> !field.getAnnotation(FlowKey.class).output())
                .filter(field -> filterMandatoryOrOptionalAndNotNull(field, node, field.getAnnotation(FlowKey.class)))
                .collect(Collectors.toList());
        return NodeUtil.extractMapFromFields(inputData, node);
    }

    private static boolean filterMandatoryOrOptionalAndNotNull(Field field, NodeContainer<? extends Node> node, FlowKey k) {
        if(k.mandatory()) return true;
        try {
            field.setAccessible(true);
            T<?> t = (T<?>) field.get(node.getC());
            return t.getTerm() != null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

