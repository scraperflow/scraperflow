package scraper.plugins.core.typechecker;

import scraper.annotations.node.FlowKey;
import scraper.api.exceptions.TemplateException;
import scraper.api.node.container.NodeContainer;
import scraper.api.specification.ScrapeInstance;
import scraper.api.template.L;
import scraper.api.template.T;
import scraper.api.template.Term;
import scraper.api.template.TypeGeneralizer;
import scraper.core.template.TemplateConstant;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.typechecker.visitors.ReplaceCapturesOrCrashVisitor;
import scraper.util.NodeUtil;
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
    public Map<String, T<?>> captures = new HashMap<>();
    public List<String> ignore = new ArrayList<>();
    private Map<L<?>, T<?>> save = new HashMap<>();

    public static List<List<NodeContainer<?>>> paths = new LinkedList<>();

    public TypeChecker() {}

    public TypeChecker(TypeChecker t) {
        this.captures = new HashMap<>(t.captures);
        this.ignore = new ArrayList<>(t.ignore);
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
            if(!(stringObjectEntry.getValue() instanceof String)) {
                throw new IllegalStateException("Non-String initial arguments not supported");
            }

            log.log(DEBUG, "<initial> {0} :: {1}", stringObjectEntry.getKey(), "String");
            L<String> loc = new L<>() {};
            loc.setLocation( new TemplateConstant<>(stringObjectEntry.getKey(), new T<>(){}) );
            env.add(loc.getLocation(), new T<String>(){});
        }

        log.log(DEBUG, "========================================");

        List<NodeContainer<?>> visited = new LinkedList<>();
        spec.getEntry().ifPresent(n -> propagate(n, env, spec, cfg, visited));
    }

    // T-Flow-Propagate
    public void propagate(NodeContainer<?> n, TypeEnvironment env, ScrapeInstance spec, ControlFlowGraph cfg, List<NodeContainer<?>> visited) {
        if(!visited.contains(n)) visited.add(n);
        if(paths.contains(visited)) {
            log.log(INFO, "Visiting cycle at {0}, skip", n.getAddress());
            return;
        }

        ignore.clear();
        captures.clear();

        // for special nodes that do very special things
        // e.g. SocketNode evaluates its template after forkDepend returns
        addNodeInfoBefore(env, n, cfg, spec, visited);

        typeNode(env, n);

        // can create sub-type checkers
        // e.g. fork join, map join
        addNodeInfo(env, n, cfg, spec, visited);

        // clear capture only after finish
        captures.clear();
        ignore.clear();

        paths.add(new LinkedList<>(visited));

        cfg.getOutgoingEdges(n.getAddress()).forEach(e -> {
            if(e.isDispatched()) {
                NodeContainer<?> nextNode = spec.getNode(e.getToAddress());
                propagate(nextNode, env.copy(), spec, cfg, new LinkedList<>(visited));
            } else {
                if(
                        e.getDisplayLabel().equalsIgnoreCase("forward") || e.isPropagate()
                ) {
                    NodeContainer<?> nextNode = spec.getNode(e.getToAddress());
                    if (e.isPropagate()) {
                        propagate(nextNode, env.copy(), spec, cfg, new LinkedList<>(visited));
                    } else {
                        propagate(nextNode, env, spec, cfg, new LinkedList<>(visited));
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
//        log.log(DEBUG, "==== Typing node {0} ({1})", n, n.getC().getClass().getSimpleName());

        getDefaultDataFlowInputTemplates(n)
                .forEach((fieldName, template) -> {
                    if(ignore.contains(fieldName)) {
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

    private void addNodeInfoBefore(TypeEnvironment env, NodeContainer<?> n, ControlFlowGraph cfg, ScrapeInstance spec, List<NodeContainer<?>> visited) {
        List<Class<?>> classesToCheck = getReverseOrderHierarchy(n);
        Collections.reverse(classesToCheck);

        for (Class<?> nodeClass : classesToCheck) {
            String controlClass = "scraper.plugins.core.typechecker.data."+nodeClass.getSimpleName()+"Data";
            try {
                Class<?> control = Class.forName(controlClass);
                Method method = control.getDeclaredMethod("infoBefore",
                        TypeChecker.class,
                        TypeEnvironment.class,
                        NodeContainer.class,
                        ControlFlowGraph.class,
                        ScrapeInstance.class,
                        List.class);
                method.invoke(null, this, env, n, cfg, spec, visited);
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

    private void addNodeInfo(TypeEnvironment env, NodeContainer<?> n, ControlFlowGraph cfg, ScrapeInstance spec, List<NodeContainer<?>> visited) {
        // log.log(DEBUG, "=== Adding node info {0}", n);

        getDefaultDataFlowOutput(n).forEach((fieldName, output) -> {
            try {
                Type tt = new ReplaceCapturesOrCrashVisitor(captures).visit(output.get());
                if (!tt.equals(output.get())) {
//                    log.log(DEBUG, "Captured types {0} ==> {1}", output.getLocation(), tt);
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
                        NodeContainer.class,
                        ControlFlowGraph.class,
                        ScrapeInstance.class,
                        List.class);
                method.invoke(null, this, env, n, cfg, spec, visited);
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
        T<?> newToken = fixpoint(List.of(token));
        env.add(term, newToken);
    }

    public T<?> fixpoint(List<T<?>> fixpoint) {
        // make mutable
        fixpoint = new ArrayList<>(fixpoint);

        boolean changed = true;
        while(changed) {
            changed = false;
            for (int i = 0; i < fixpoint.size(); i++) {
                T<?> t = fixpoint.get(i);

                Type tt = new ReplaceCapturesOrCrashVisitor(captures).visit(t.get());

                if(!t.equalsType(new T<>(tt){})){
                    changed = true;
                    fixpoint.set(i, new T<>(tt){});
                }

                if(t.equalsType(new T<>(tt){}) && !tt.getTypeName().equalsIgnoreCase(t.getTypeString())){
                    changed = true;
                    fixpoint.set(i, new T<>(tt){});
                }
            }
        }

        // every term has to evaluate to the same Type T
        T<?> last = null;
        for (T<?> t : fixpoint) {
            if(last == null) { last = t; continue; }

            Type newToken = new TypeGeneralizer( captures ){}.visit(last.get(), t.get());
            if(newToken == null) {
                throw new TemplateException("Terms type variable resolves to different types: " + fixpoint);
            } else {
                last = new T<>(newToken){};
            }
        }

        return last;
    }

    public T<?> resolve(String var) {
        T<?> current = captures.get(var);
        T<?> last = captures.get(var);
        while(last != null) {
            current = last;
            last = captures.get(current.getTypeString());
            if(current == last) break;
        }

        return current;
    }

    public T<?> putIfNotConflicting(String typeString, T<?> token) {
        T<?> knownToken = resolve(typeString);
        if(knownToken == null) {
            log.log(DEBUG, "{0} ~> {1}", typeString, token);
            return putAndResolve(typeString, token);
        }

        Type newToken = new TypeGeneralizer( captures ){}.visit(knownToken.get(), token.get());

        if(newToken != null) {
            log.log(DEBUG, "{0} ~> {1}", typeString, newToken);
            return putAndResolve(typeString, new T<>(newToken){});
        }

        throw new TemplateException("Capture at " + typeString + " :: "+ knownToken.getTypeString() + " does not match the to-put capture " + token.getTypeString());
    }

    private T<?> putAndResolve(String capt, T<?> known) {
        if(known.getTerm() != null && known.getTerm().isTypeVariable()) {
            // put most precise
            T<?> resolved = resolve(known.getTypeString());
            T<?> precise = fixpoint(List.of(captures.getOrDefault(capt, known), resolved));

            log.log(DEBUG, "{0} ~> {1}", capt, precise.getTypeString());
            captures.put(capt, precise);
            return precise;
        } else {
            log.log(DEBUG, "{0} ~> {1}", capt, known.getTypeString());
            captures.put(capt, known);
            return known;
        }
    }

    public void ignoreField(String expected) {
        ignore.add(expected);
    }

    public void unignoreField(String expected) {
        ignore.remove(expected);
    }

    public void save(L<?> put, TypeEnvironment env) {
        save.put(put, env.get(put.getLocation()));
    }

    public void restore(L<?> putBody) {
        save.remove(putBody);
    }
}
