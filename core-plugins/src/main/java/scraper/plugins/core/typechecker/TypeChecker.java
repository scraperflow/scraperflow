package scraper.plugins.core.typechecker;

import scraper.annotations.node.FlowKey;
import scraper.api.exceptions.TemplateException;
import scraper.api.exceptions.ValidationException;
import scraper.api.node.container.NodeContainer;
import scraper.api.specification.ScrapeInstance;
import scraper.api.template.L;
import scraper.api.template.T;
import scraper.api.template.Term;
import scraper.core.template.TemplateConstant;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.typechecker.visitors.ReplaceCapturesOrCrashVisitor;
import scraper.util.NodeUtil;
import scraper.util.TemplateUtil;
import scraper.utils.ClassUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.*;
import static scraper.plugins.core.flowgraph.FlowUtil.getReverseOrderHierarchy;

public class TypeChecker {
    private static final System.Logger log = System.getLogger("TypeChecker");

    public Map<ControlFlowEdge, TypeEnvironment> edgeInfo = new HashMap<>();
    public List<List<NodeContainer<?>>> paths = new LinkedList<>();

    public TypeChecker() {}

    public TypeChecker(TypeChecker t) {
        this.edgeInfo = new HashMap<>(t.edgeInfo);
    }

    /*
     * =====================
     * Flow Typing Rules
     * =====================
     */

    // T-Flow
    public void typeTaskflow(ScrapeInstance spec, ControlFlowGraph cfg) {

        TypeEnvironment env = new TypeEnvironment();
        // args add initial arguments
        for (Map.Entry<String, Object> stringObjectEntry : spec.getEntryArguments().entrySet()) {
            L<?> loc = new L<>() {};
            loc.setLocation( new TemplateConstant<>(stringObjectEntry.getKey(), new T<>(){}) );
            if(!(stringObjectEntry.getValue() instanceof String)) {
                try {
                    Term<?> argsType = TemplateUtil.inferTemplate(stringObjectEntry.getValue());
                    env.add(loc.getLocation(), argsType.getToken());
                } catch (ValidationException e) {
                    throw new TemplateException(e, "Could not infer args type");
                }
            } else {
                log.log(DEBUG, "<initial> {0} :: {1}", stringObjectEntry.getKey(), "String");
                env.add(loc.getLocation(), new T<String>(){});
            }
        }

        log.log(DEBUG, "========================================");

        List<NodeContainer<?>> visited = new LinkedList<>();
        spec.getEntry().ifPresent(n -> propagate(null, n, env, spec, cfg, visited));
    }

    // T-Flow-Propagate
    public void propagate(ControlFlowEdge incomingEdge, NodeContainer<?> n, TypeEnvironment env, ScrapeInstance spec, ControlFlowGraph cfg, List<NodeContainer<?>> visited) {
        log.log(TRACE, "=================== {0}", incomingEdge);
        if(incomingEdge != null) {
            TypeEnvironment knownTypeEnv = edgeInfo.getOrDefault(incomingEdge, new TypeEnvironment());
            if(env.greaterThan(knownTypeEnv)) {
                log.log(DEBUG, "Adding information to edge {0}", incomingEdge);
                knownTypeEnv.merge(env);
                edgeInfo.put(incomingEdge, knownTypeEnv);
            }
        }

        if(visited.contains(n)) {
            log.log(DEBUG, "Skip cycle at {0}, add info to edge", n.getAddress());
            return;
        }

        visited.add(n);

        if(paths.contains(visited)) return;
        paths.add(new LinkedList<>(visited));

        // merge all incoming type information in, cycles
        cfg.getIncomingEdges(n.getAddress()).forEach(otherIncomingEdge -> {
            if(incomingEdge == otherIncomingEdge) {
                return;
            }
            if(edgeInfo.containsKey(otherIncomingEdge)) {
                log.log(DEBUG, "previous type information added of {0}", otherIncomingEdge);
                env.merge(edgeInfo.get(otherIncomingEdge));
            }
        });

        env.ignore.clear();
        env.captures.clear();

        // for special nodes that do very special things
        // e.g. SocketNode evaluates its template after forkDepend returns
        addNodeInfoBefore(env, n, incomingEdge, cfg, spec, visited);

        typeNode(env, n);

        // can create sub-type checkers
        // e.g. fork join, map join
        addNodeInfo(env, n, incomingEdge, cfg, spec, visited);

        // clear capture only after finish
        env.captures.clear();
        env.ignore.clear();

        cfg.getOutgoingEdges(n.getAddress()).forEach(e -> {
            if(e.isDispatched()) {
                NodeContainer<?> nextNode = spec.getNode(e.getToAddress());
                propagate(e, nextNode, env.copy(), spec, cfg, new LinkedList<>(visited));
            } else {
                if(
                        e.getDisplayLabel().equalsIgnoreCase("forward") || e.isPropagate()
                ) {
                    NodeContainer<?> nextNode = spec.getNode(e.getToAddress());
                    if (e.isPropagate()) {
                        propagate(e, nextNode, env.copy(), spec, cfg, new LinkedList<>(visited));
                    } else {
                        propagate(e, nextNode, env, spec, cfg, new LinkedList<>(visited));
                    }
                } else {
                    log.log(DEBUG, "Not propagating: " + e.getDisplayLabel());
                }
            }
        });
    }

    /*
     * =====================
     * Node Typing Rules
     * =====================
     */

    // T-Template
    public T<?> typeTemplate(TypeEnvironment env, T<?> t) {
        return new TypeRules(env, this).typeTemplate(t);
    }

    // T-Node
    private void typeNode(TypeEnvironment env, NodeContainer<?> n) {
        log.log(DEBUG, "==== Typing node {0} ({1})", n, n.getC().getClass().getSimpleName());

        getDefaultDataFlowInputTemplates(n)
                .forEach((fieldName, template) -> {
                    if(env.ignore.contains(fieldName)) {
//                        log.log(DEBUG, "Ignoring field as requested: {0}", fieldName);
                        return;
                    }

                    try {
                        typeTemplate(env, template);
                    } catch (TemplateException e) {
                        log.log(ERROR,"{0} type error for field {1}: {2}", n, fieldName, e.getMessage());
                        throw e;
                    }
                        }
                );
    }

    /*
     * =====================
     * Auxiliary
     * =====================
     */

    private void addNodeInfoBefore(TypeEnvironment env, NodeContainer<?> n, ControlFlowEdge inc, ControlFlowGraph cfg, ScrapeInstance spec, List<NodeContainer<?>> visited) {
        List<Class<?>> classesToCheck = getReverseOrderHierarchy(n);
        Collections.reverse(classesToCheck);

        for (Class<?> nodeClass : classesToCheck) {
            String controlClass = "scraper.plugins.core.typechecker.data."+nodeClass.getSimpleName()+"Data";
            try {
                Class<?> control = Class.forName(controlClass);
                Method method = control.getDeclaredMethod("infoBefore",
                        TypeChecker.class,
                        TypeEnvironment.class,
                        ControlFlowEdge.class,
                        NodeContainer.class,
                        ControlFlowGraph.class,
                        ScrapeInstance.class,
                        List.class);
                method.invoke(null, this, env, inc, n, cfg, spec, visited);
            } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException ignored) {
//                System.out.println("[Skip] Could not find control for " + nodeClass + ": " + controlClass);
            } catch (InvocationTargetException e) {
                if(e.getCause() instanceof TemplateException) {
                    log.log(ERROR,"{0} type error for field: {1}", n, e.getCause().getMessage());
                    throw (TemplateException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        }
    }

    private void addNodeInfo(TypeEnvironment env, NodeContainer<?> n, ControlFlowEdge inc, ControlFlowGraph cfg, ScrapeInstance spec, List<NodeContainer<?>> visited) {
        // log.log(DEBUG, "=== Adding node info {0}", n);

        getDefaultDataFlowOutput(n).forEach((fieldName, output) -> {
            try {
                Type tt = new ReplaceCapturesOrCrashVisitor(env.captures).visit(output.get());
                if (!tt.equals(output.get())) {
                    log.log(DEBUG, "Captured types {0} ==> {1}", output.getLocation(), tt);
                    Term<String> loc = output.getLocation();
                    output = new L<>(tt){};
                    output.setLocation(loc);
                }

                {
                    log.log(DEBUG, "<{0}> {1} :: {2}", n.getAddress(), output.getLocation(), output.getTarget().getTypeString());
                    env.add(output.getLocation(), new T<>(output.get()){});
                }
            } catch (TemplateException e){
                log.log(ERROR,"{0} type error for field {1}: {2}", n, fieldName, e.getMessage());
                throw e;
            }
                }
        );



        List<Class<?>> classesToCheck = getReverseOrderHierarchy(n);
        Collections.reverse(classesToCheck);

        for (Class<?> nodeClass : classesToCheck) {
            String controlClass = "scraper.plugins.core.typechecker.data."+nodeClass.getSimpleName()+"Data";
            try {
                Class<?> control = Class.forName(controlClass);
                Method method = control.getDeclaredMethod("infoAfter",
                        TypeChecker.class,
                        TypeEnvironment.class,
                        ControlFlowEdge.class,
                        NodeContainer.class,
                        ControlFlowGraph.class,
                        ScrapeInstance.class,
                        List.class);
                method.invoke(null, this, env, inc, n, cfg, spec, visited);
            } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException ignored) {
//                System.out.println("[Skip] Could not find control for " + nodeClass + ": " + controlClass);
            } catch (InvocationTargetException e) {
                if(e.getCause() instanceof TemplateException) {
                    log.log(ERROR,"{0} type error for field: {1}", n, e.getCause().getMessage());
                    throw (TemplateException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        }
    }

    public static Map<String, T<?>> getDefaultDataFlowInputTemplates(NodeContainer<?> node) {
        List<Class<?>> classesToCheck = getReverseOrderHierarchy(node);

        return classesToCheck.stream().map(c -> {
            List<Field> outputData = ClassUtil.getAllFields(new LinkedList<>(), c).stream()
                    // only templates
                    .filter(field -> field.getType().equals(T.class))
                    // only annotated by flow keys
                    .filter(field -> field.getAnnotation(FlowKey.class) != null)
                    .collect(Collectors.toList());

            return outputData.stream()
                    .map(field -> {
                        String name = field.getName();
                        field.setAccessible(true);
                        try {
                            return Map.entry(name, (T<?>) field.get(node.getC()));
                        } catch (Exception e) {
                            try {
                                return Map.entry(name, (T<?>) field.get(node));
                            } catch (Exception e2) {
                                throw new RuntimeException(e2);
                            }
                        }
                    }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        })
                .collect(Collectors.toList())
                .stream()
                .flatMap(m -> m.entrySet().stream())
                .distinct()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, L<?>> getDefaultDataFlowOutput(NodeContainer<?> node) {
        List<Field> outputData = ClassUtil.getAllFields(new LinkedList<>(), node.getC().getClass()).stream()
                // only templates
                .filter(field -> field.getType().equals(L.class))
                // only annotated by flow keys
                .filter(field -> field.getAnnotation(FlowKey.class) != null)
                .collect(Collectors.toList());
        return NodeUtil.extractMapFromFields(outputData, node);
    }

    public void add(TypeEnvironment env, Term<?> term, T<?> token) {
        T<?> newToken = env.fixpoint(List.of(token));
        env.add(term, newToken);
    }


    static class P<X,Y> {
        X fst; Y snd;
        P(X fst, Y snd){this.fst = fst; this.snd = snd;}
        static <X,Y> P<X,Y> p(X fst, Y snd) { return new P<>(fst, snd); }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            P<?, ?> p = (P<?, ?>) o;
            return fst.equals(p.fst) && snd.equals(p.snd);
        }

        @Override public int hashCode() { return Objects.hash(fst, snd); }
    }
}
