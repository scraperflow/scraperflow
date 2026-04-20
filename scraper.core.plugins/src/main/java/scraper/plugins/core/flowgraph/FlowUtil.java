package scraper.plugins.core.flowgraph;

import scraper.annotations.NotNull;
import scraper.annotations.Flow;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.Address;
import scraper.api.NodeAddress;
import scraper.api.NodeContainer;
import scraper.api.StreamNodeContainer;
import scraper.api.Node;
import scraper.api.ScrapeInstance;
import scraper.api.T;
import scraper.core.AbstractStreamNode;
import scraper.core.IdentityEvaluator;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.ControlFlowNode;
import scraper.plugins.core.flowgraph.impl.ControlFlowGraphImpl;
import scraper.plugins.core.flowgraph.impl.ControlFlowNodeImpl;
import scraper.util.NodeUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static scraper.plugins.core.flowgraph.impl.ControlFlowEdgeImpl.edge;

@SuppressWarnings("unchecked") //convention
public class FlowUtil {

    @NotNull
    public static ControlFlowGraph generateControlFlowGraph(ScrapeInstance instance) {
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

        // special case 1: forward contract
        Optional<NodeContainer<? extends Node>> goTo = node.getGoTo();
        if(goTo.isPresent() && node.isForward()) {
            // special case 2: stream node has no forward in "collect: false" mode
            if(node instanceof StreamNodeContainer) {
                try {
                    Field collect = node.getClass().getSuperclass().getDeclaredField("collect");
                    collect.setAccessible(true);
                    if( ((Boolean) collect.get(node)) ) {
                        output.add(edge(node.getAddress(), goTo.get().getAddress(), "forward"));
                    }
                } catch (Exception e) { throw new RuntimeException(e); }
            } else {
                output.add(edge(node.getAddress(), goTo.get().getAddress(), "forward"));
            }
        }

        for (Class<?> nodeClass : classesToCheck) {

            BiFunction<Object, Flow, List<ControlFlowEdge>> handleAddress = (obj, annot) -> {
                if(obj instanceof T) {
                    FlowMapImpl o = new FlowMapImpl();
                    obj = o.evalIdentity(((T<?>) obj));
                }

                if(obj instanceof Address) {
                    Address target = ((Address) obj);
                    NodeContainer<? extends Node> map = NodeUtil.getTarget(node.getAddress(), target, instance);
                    return List.of(edge(
                            node.getAddress(), map.getAddress(), annot.label(), true
                    ));
                } else if(obj instanceof List) {
                    List<Address> targets = ((List<Address>) obj);

                    List<ControlFlowEdge> edges = new ArrayList<>();
                    int i = 0;
                    for (Address target : targets) {
                        NodeContainer<? extends Node> n = NodeUtil.getTarget(node.getAddress(), target, instance);
                        edges.add(edge( node.getAddress(), n.getAddress(), annot.label()+(annot.enumerate()?i:""), false, false ));
                        i++;
                    }

                    return edges;
                } else if(obj instanceof Map) {
                    Map<String, Address> targets = ((Map<String, Address>) obj);
                    return targets.entrySet().stream().map(e -> {
                        Address t = e.getValue();
                        NodeContainer<? extends Node> n = NodeUtil.getTarget(node.getAddress(), t, instance);
                        return edge( node.getAddress(), n.getAddress(), e.getKey(), false, false );
                    }).collect(Collectors.toList());
                } else {
                    throw new RuntimeException("Only Address, List, or Map supported");
                }
            };

            Stream<List<ControlFlowEdge>> stremm = Arrays.stream(nodeClass.getDeclaredFields())
                    .filter(f -> f.isAnnotationPresent(Flow.class))
                    .map(f -> {
                        f.setAccessible(true);
                        try {
                            if (f.get(node) != null) {
                                return handleAddress.apply(f.get(node), f.getAnnotation(Flow.class));
                            }
                        } catch (Exception ignored) { }
                        try {
                            if (f.get(node.getC()) != null) {
                                return handleAddress.apply(f.get(node.getC()), f.getAnnotation(Flow.class));
                            }
                        } catch (Exception ignored) { }
                        return List.of();
                    });

            List<ControlFlowEdge> collected = stremm.flatMap(List::stream).collect(Collectors.toList());

            collected.forEach(cfg::addEdge);
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

    public static List<Address> getFieldsForClassAnnotated(Class<Flow> flowClass, Object instance, Class<?> nodeClass) throws IllegalAccessException {
        List<Field> flowFields = Arrays.stream(nodeClass.getDeclaredFields())
                .filter(f -> {
                    f.setAccessible(true);
                    Flow an = f.getAnnotation(flowClass);
                    return an != null;
                }).collect(Collectors.toList());

        List<Address> objs = new ArrayList<>();

        for (Field flow : flowFields) {
            Object inst = flow.get(instance);
            if(inst instanceof Address) {
                objs.add((Address) inst);
            }

            // TODO eval identity
            if(inst instanceof T) {
                T<?> casted = ((T<?>) inst);
                Object evaled = new IdentityEvaluator().evalIdentity(casted);

                List<Address> flist = filt(evaled);

                objs.addAll(flist);
            }
        }

        return objs;
    }

    public static List<Address> filt(Object object) {
        List<Address> filt = new LinkedList<>();

        if(object instanceof Address){
            filt.add(((Address) object));
        }
        if(object instanceof List){
            filt.addAll(filtList((List<?>) object));
        }
        if(object instanceof Map){
            filt.addAll(filtMap((Map<String, ?>) object));
        }

        return filt;
    }

    public static List<Address> filtList(List<?> objects) {
        List<Address> filt = new LinkedList<>();
        for (Object object : objects) {
            filt.addAll(filt(object));
        }
        return filt;
    }

    private static List<Address> filtMap(Map<String, ?> objects) {
        List<Address> filt = new LinkedList<>();
        for (Object object : objects.values()) {
            filt.addAll(filt(object));
        }
        return filt;
    }
}

