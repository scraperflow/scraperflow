package scraper.plugins.core.flowgraph;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.api.node.Address;
import scraper.api.node.NodeAddress;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.api.template.L;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.ControlFlowNode;
import scraper.plugins.core.flowgraph.impl.ControlFlowGraphImpl;
import scraper.plugins.core.flowgraph.impl.ControlFlowNodeImpl;
import scraper.util.NodeUtil;
import scraper.utils.ClassUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked") //convention
public class FlowUtil {

    public static ControlFlowGraph generateControlFlowGraph(ScrapeInstance instance) {
        return generateControlFlowGraph(instance, false);
    }

    @NotNull
    public static ControlFlowGraph generateControlFlowGraph(ScrapeInstance instance, boolean realControlFlow) {
        visited.clear();

        ControlFlowGraphImpl cfg = new ControlFlowGraphImpl();

        Map<NodeAddress, NodeContainer<? extends Node>> nodes = new HashMap<>();
        instance
                .getRoutes()
                .forEach(((address, nodeContainer) -> {
            if (address.isAbsolute()) {
                if (!nodes.containsKey(nodeContainer.getAddress())) {
                    nodes.put(nodeContainer.getAddress(), nodeContainer);
                }
            }
        }));
        nodes.forEach((adr, node) -> handleNode(cfg, instance, node));


//
//        if (realControlFlow && instance.getEntry().isPresent()) {
//            Address addr = instance.getEntry().get().getAddress();
//            propagateRealControlFlow(cfg, addr, instance);
//        }

        return cfg;
    }

    static Collection<Address> visited = new HashSet<>();
    private static void propagateRealControlFlow(ControlFlowGraphImpl cfg, Address addr, ScrapeInstance instance) {
        if(visited.contains(addr)) return;
        visited.add(addr);


        ControlFlowNode n = cfg.getNodes().get(addr);
        String type = (n == null ? "none" : n.getType());

        // assume only direct type handles control flow changes
        String controlClass = "scraper.plugins.core.flowgraph.control."+type+"Control";
        try {
            Class<?> control = Class.forName(controlClass);
            Method method = control.getDeclaredMethod("propagate", NodeContainer.class, ControlFlowGraph.class, ScrapeInstance.class);
            method.invoke(null, instance.getNode(addr).get(), cfg, instance);
        } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException ignored) {
//                System.out.println("[Skip] Could not find control for " + nodeClass + ": " + controlClass);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        cfg.getOutgoingEdges(addr).forEach(o -> propagateRealControlFlow(cfg, o.getToAddress(), instance) );
    }

    private static void handleNode(ControlFlowGraphImpl cfg, ScrapeInstance instance, NodeContainer<? extends Node> node) {
        ControlFlowNode cfnode = new ControlFlowNodeImpl(node);
        cfg.addNode(node.getAddress(), cfnode);

        List<Class<?>> classesToCheck = getReverseOrderHierarchy(node);

        List<ControlFlowEdge> output = new LinkedList<>();

        for (Class<?> nodeClass : classesToCheck) {
            String controlClass = "scraper.plugins.core.flowgraph.control."+nodeClass.getSimpleName()+"Control";
            try {
                Class<?> control = Class.forName(controlClass);
                Method method = control.getDeclaredMethod("getOutput", List.class, NodeContainer.class, ScrapeInstance.class);
                output = (List<ControlFlowEdge>) method.invoke(null, output, node, instance);
            } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException ignored) {
//                System.out.println("[Skip] Could not find control for " + nodeClass + ": " + controlClass);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        output.forEach(cfg::addEdge);
    }

    public static List<Class<?>> getReverseOrderHierarchy(NodeContainer<?> node) {
        List<Class<?>> result = new LinkedList<>();
        result.add(node.getC().getClass());
        Class<?> current = node.getClass();
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
        return Optional.ofNullable((T) f.get(instance));
    }

    public static <T> Optional<T> getFieldForClass(String field, Object instance, Class<?> clazz) throws Exception {
        Field f = clazz.getDeclaredField(field);
        f.setAccessible(true);
        return Optional.ofNullable((T) f.get(instance));
    }
}

