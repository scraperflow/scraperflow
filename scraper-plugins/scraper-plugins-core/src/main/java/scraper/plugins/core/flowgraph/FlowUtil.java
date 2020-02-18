package scraper.plugins.core.flowgraph;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.core.Template;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.ControlFlowNode;
import scraper.plugins.core.flowgraph.impl.ControlFlowGraphImpl;
import scraper.plugins.core.flowgraph.impl.ControlFlowNodeImpl;
import scraper.plugins.core.flowgraph.impl.DataFlowGraphImpl;
import scraper.plugins.core.flowgraph.impl.DataFlowNodeImpl;
import scraper.util.NodeUtil;
import scraper.utils.ClassUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class FlowUtil {

    @NotNull
    public static ControlFlowGraph generateControlFlowGraph(ScrapeInstance instance) {
        ControlFlowGraphImpl cfg = new ControlFlowGraphImpl();

        instance.getRoutes().forEach(((address, nodeContainer) -> {
            if (address.isAbsolute()) {
                handleNode(cfg, instance, nodeContainer);
            }
        }));

        return cfg;
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

    private static void handleNode(ControlFlowGraphImpl cfg, ScrapeInstance instance, NodeContainer<? extends Node> node) {
        ControlFlowNode cfnode = new ControlFlowNodeImpl(node.getAddress());
        cfg.addNode(node.getAddress(), cfnode);

        List<Class> classesToCheck = getReverseOrderHierarchy(node);

        List<ControlFlowEdge> output = new LinkedList<>();

        for (Class nodeClass : classesToCheck) {
            String controlClass = "scraper.plugins.core.flowgraph.control."+nodeClass.getSimpleName()+"Control";
            try {
                Class<?> control = Class.forName(controlClass);
                Method method = control.getDeclaredMethod("getOutput", List.class, NodeContainer.class, ScrapeInstance.class);
                //noinspection unchecked
                output = (List<ControlFlowEdge>) method.invoke(null, output, node, instance);
            } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException ignored) {
                System.out.println("[Skip] Could not find control for " + nodeClass + ": " + controlClass);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        output.forEach(cfg::addEdge);
    }

    private static void handleNodeDfg(DataFlowGraphImpl dfg, ScrapeInstance instance, NodeContainer<? extends Node> node) {
        DataFlowNodeImpl cfnode = new DataFlowNodeImpl(node.getAddress());
        dfg.addNode(node.getAddress(), cfnode);

        List<Class> classesToCheck = getReverseOrderHierarchy(node);

        // default input
        Map<String, String> defaultsInput = getDefaultDataFlowInput(node);
        Map<String, String> input = new HashMap<>(defaultsInput);

        // default output
        Map<String, String> defaults = getDefaultDataFlowOutput(node);
        Map<String, String> output = new HashMap<>(defaults);

        for (Class nodeClass : classesToCheck) {
            String controlClass = "scraper.plugins.core.flowgraph.data."+nodeClass.getSimpleName()+"Data";
            try {
                Class<?> control = Class.forName(controlClass);
                Method method = control.getDeclaredMethod("getOutput", Map.class, NodeContainer.class, ScrapeInstance.class);
                //noinspection unchecked
                output = (Map<String, String>) method.invoke(null, output, node, instance);

                Method method2 = control.getDeclaredMethod("getInput", Map.class, NodeContainer.class, ScrapeInstance.class);
                //noinspection unchecked
                input = (Map<String, String>) method2.invoke(null, input, node, instance);
            } catch (Exception ignored) {
                System.out.println("[Skip] Could not find data for " + nodeClass+": " + controlClass);
            }
        }

        output.forEach(cfnode::addProduce);
        input.forEach(cfnode::addConsume);

        dfg.addNode(node.getAddress(), cfnode);
    }

    private static List<Class> getReverseOrderHierarchy(NodeContainer<? extends Node> node) {
        List<Class> result = new LinkedList<>();
        result.add(node.getC().getClass());
        Class current = node.getClass();
        while(!current.getName().toLowerCase().contains("java.lang.object")) {
            if(current.getSimpleName().toLowerCase().contains("node")) {
                result.add(current);
            }
            current = current.getSuperclass();
        }

        Collections.reverse(result);

        return result;
    }

    public static <T> Optional<T> getField(String field, Object instance) throws Exception {
        Field f = instance.getClass().getDeclaredField(field);
        f.setAccessible(true);
        //noinspection unchecked
        return Optional.ofNullable((T) f.get(instance));
    }

    public static <T> Optional<T> getFieldForClass(String field, Object instance, Class<?> clazz) throws Exception {
        Field f = clazz.getDeclaredField(field);
        f.setAccessible(true);
        //noinspection unchecked
        return Optional.ofNullable((T) f.get(instance));
    }

    private static Map<String, String> getDefaultDataFlowOutput(NodeContainer<? extends Node> node) {
        //noinspection RedundantOperationOnEmptyContainer what is this warning about???
        List<Field> outputData = ClassUtil.getAllFields(new LinkedList<>(), node.getClass()).stream()
                // only templates
                .filter(field -> field.getType().isAssignableFrom(Template.class))
                // only annotated by flow keys
                .filter(field -> field.getAnnotation(FlowKey.class) != null)
                // only output
                .filter(field -> field.getAnnotation(FlowKey.class).output())
                .collect(Collectors.toList());
        return NodeUtil.extractMapFromFields(outputData, node);
    }

    private static Map<String, String> getDefaultDataFlowInput(NodeContainer<? extends Node> node) {
        //noinspection RedundantOperationOnEmptyContainer what is this warning about???
        List<Field> inputData = ClassUtil.getAllFields(new LinkedList<>(), node.getClass()).stream()
                // only templates
                .filter(field -> field.getType().isAssignableFrom(Template.class))
                // only annotated by flow keys
                .filter(field -> field.getAnnotation(FlowKey.class) != null)
                // only input
                .filter(field -> !field.getAnnotation(FlowKey.class).output())
                .collect(Collectors.toList());
        return NodeUtil.extractMapFromFields(inputData, node);
    }
}

// old get input flow method, unused
//
//    @NotNull
//    @Override
//    public List<ControlFlowEdge> getInput() {
//        return getJobPojo().getGraph(getGraphKey())
//                .stream()
//                // every output of every node is checked
//                .map(ControlFlow::getOutput)
//                .flatMap(Collection::stream)
//                // if target address is included, then the input to this node is that node
//                .filter(edge -> edge.getToAddress().equals(getAddress()))
//                // flip the from/to addresses for every filtered edge
//                .map((Function<ControlFlowEdge, ControlFlowEdge>) origin ->
//                        new ControlFlowEdgeImpl(origin.getToAddress(), origin.getFromAddress(), origin.getDisplayLabel(),
//                                origin.isMultiple(), origin.isDispatched()))
//                .collect(Collectors.toList());
//    }

