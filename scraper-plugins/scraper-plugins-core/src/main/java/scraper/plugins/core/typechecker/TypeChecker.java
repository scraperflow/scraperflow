package scraper.plugins.core.typechecker;

import org.slf4j.Logger;
import scraper.annotations.node.FlowKey;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.flow.impl.SpecializeException;
import scraper.api.node.container.NodeContainer;
import scraper.api.specification.ScrapeInstance;
import scraper.api.template.*;
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

import static java.util.Map.entry;
import static scraper.plugins.core.flowgraph.FlowUtil.getReverseOrderHierarchy;

public class TypeChecker extends DefaultVisitor<Map<String, T<?>>> {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger("TypeChecker");

    public TypeEnvironment env = new TypeEnvironment();
    public Map<String, T<?>> captures = new HashMap<>();

    /*
     * =====================
     * Template Typing Rules
     * =====================
     */

    // T-Template
    public void typeTemplate(T<?> simpleString) {
        if(simpleString.getTerm() != null) simpleString.getTerm().accept(this);
        else {
            // This is a corner case for optional template evaluation, carry the type variables on
            Map<String, T<?>> captures = FlowMapImpl.checkGenericTypeAndCapture(this.captures, simpleString.get(), simpleString);
            this.captures.putAll(captures);
        }
    }

    // T-Constant-Template
    @Override public Map<String, T<?>> visitPrimitive(Primitive<?> primitive) {
        log.trace("Checking primitive {} with target type {}", primitive, primitive.getToken().get().getTypeName());
        // eval is also type check, but not needed
        Object evalObj = primitive.eval();

        // capture type variables
        T<?> known = FlowMapImpl.inferTypeToken(evalObj);
        Map<String, T<?>> captures = FlowMapImpl.checkGenericTypeAndCapture(this.captures, known.get(), primitive.getToken());
        if(!captures.isEmpty()) log.debug("Capturing {}", captures);
        this.captures.putAll(captures);

        return captures;
    }

    // T-Key-Lookup-Template
    @Override public Map<String, T<?>> visitFlowKeyLookup(FlowKeyLookup<?> mapKey) {
        { // (1) check string type
            mapKey.getKeyLookup().accept(this);
        }

        { // (2) check if inner template is in env
            Term<String> keyTemplate = mapKey.getKeyLookup();
            log.debug("Checking env({}) = {} rule", keyTemplate, mapKey.getToken().get());

            var known = env.get(keyTemplate);

            if(known == null)
                throw new TemplateException("Template term not in environment: " + keyTemplate);

            try {
                Map<String, T<?>> captures = FlowMapImpl.checkGenericTypeAndCapture(this.captures, known, mapKey.getToken());
                if(!captures.isEmpty()) log.debug("Capturing {}", captures);
                this.captures.putAll(captures);
            } catch (SpecializeException e) {
                log.info("Specializing {} -> {}", known, mapKey.getToken());
                env.add(keyTemplate, mapKey.getToken().get());
            } catch (Exception e) {
                log.error("Could not type key lookup {}: {}", mapKey.getKeyLookup(), e.getMessage());
                throw e;
            }


            return captures;
        }
    }

    // T-List-Template
    @Override public Map<String, T<?>> visitListTerm(ListTerm<?> list) {
        if(list.isTypeVariable()) {
            if(list.getTerms().isEmpty()) {
                Map<String, T<?>> captures = FlowMapImpl.checkGenericTypeAndCapture(this.captures, list.getToken().get(), list.getToken());
                Map<String, T<?>> toListTransform = captures
                        .entrySet().stream()
                        .map(e -> entry(e.getKey(), TemplateUtil.listOf(e.getValue())) )
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                this.captures.putAll(toListTransform);
                if(!toListTransform.isEmpty()) log.debug("Capturing {}", toListTransform);
            } else {
                Map<String, T<?>> allElements = new HashMap<>();
                list.getTerms().forEach(term -> {
                    Map<String, T<?>> captured = term.accept(this);
                    Map<String, T<?>> toListTransform = captured
                            .entrySet().stream()
                            .map(e -> entry(e.getKey(), TemplateUtil.listOf(e.getValue())) )
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


                    for (var e : toListTransform.entrySet()) {
                        if(allElements.get(e.getKey()) != null) {
                            if(!allElements.get(e.getKey()).equals(e.getValue()))
                                throw new TemplateException("Elements in list have a different type");
                        }
                    }
                    allElements.putAll(toListTransform);
                    if(!toListTransform.isEmpty()) log.debug("Capturing {}", toListTransform);
                });

                // capture after list has been processed
                this.captures.putAll(allElements);
                if(!allElements.isEmpty()) log.debug("Capturing {}", allElements);
                return allElements;
            }
        } else {
            list.getTerms().forEach(r -> r.accept(this));
        }
        return Map.of();
    }

    // T-Map-Template
    @Override public Map<String, T<?>> visitMapTerm(MapTerm<?> mapTerm) {
        if(mapTerm.isTypeVariable()) {
            if(mapTerm.getTerms().isEmpty()){
                Map<String, T<?>> captures = FlowMapImpl.checkGenericTypeAndCapture(this.captures, mapTerm.getToken().get(), mapTerm.getToken());
                Map<String, T<?>> toMapTransform = captures
                        .entrySet().stream()
                        .map(e -> entry(e.getKey(), TemplateUtil.mapOf(new T<String>(){}, e.getValue())) )
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                this.captures.putAll(toMapTransform);
                if(!toMapTransform.isEmpty()) log.debug("Capturing {}", toMapTransform);
            } else {
                Map<String, T<?>> allElements = new HashMap<>();
                mapTerm.getTerms().forEach((key, term) -> {
                    Map<String, T<?>> captured = term.accept(this);
                    Map<String, T<?>> toMapTransforms = captured
                            .entrySet().stream()
                            .map(e -> entry(e.getKey(), TemplateUtil.mapOf(new T<String>(){},e.getValue())) )
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    for (var e : toMapTransforms.entrySet()) {
                        if(allElements.get(e.getKey()) != null) {
                            if(!allElements.get(e.getKey()).equals(e.getValue()))
                                throw new TemplateException("Elements in map have a different type");
                        }
                    }

                    allElements.putAll(toMapTransforms);
                    if(!allElements.isEmpty()) log.debug("Capturing {}", allElements);
                });

                // capture after list has been processed
                this.captures.putAll(allElements);
                if(!allElements.isEmpty()) log.debug("Capturing {}", allElements);
            }
        } else {
            mapTerm.getTerms().forEach((k,v) -> v.accept(this));
        }
        return Map.of();
    }

    // T-Map-Lookup-Template
    @Override public Map<String, T<?>> visitMapLookup(MapLookup<?> mapLookup) {
        mapLookup.getMapObjectTerm().accept(this);
        mapLookup.getKeyTerm().accept(this);
        return Map.of();
    }

    // T-List-Lookup
    @Override public Map<String, T<?>> visitListLookup(ListLookup<?> listLookup) {
        listLookup.getIndexTerm().accept(this);
        listLookup.getListObjectTerm().accept(this);
        return Map.of();
    }

    // T-Concat-Lookup
    @Override public Map<String, T<?>> visitConcatenation(Concatenation concat) {
        concat.getConcatenationTerms().forEach(e -> e.accept(this));
        return Map.of();
    }


    /*
     * =====================
     * Flow Typing Rules
     * =====================
     */

    public void typeTaskflow(ScrapeInstance spec, ControlFlowGraph cfg) {
        Set<NodeContainer<?>> visited = new HashSet<>();
        spec.getEntry().ifPresent(n -> propagate(n, env, spec, cfg, visited));
    }

    public void propagate(NodeContainer<?> n, TypeEnvironment env, ScrapeInstance spec, ControlFlowGraph cfg, Set<NodeContainer<?>> visited) {
        if(visited.contains(n)) return;
        visited.add(n);

        captures.clear();

        typeNode(n);

        addNodeInfo(n, cfg, spec, visited);

        captures.clear();

        cfg.getOutgoingEdges(n.getAddress()).forEach(e -> {
            NodeContainer<?> nextNode = spec.getNode(e.getToAddress());
            propagate(nextNode, env.copy(), spec, cfg, visited);
        });
    }

    /*
     * =====================
     * Node Typing Rules
     * =====================
     */

    private void typeNode(NodeContainer<?> n) {
        log.debug("==== Typing node {} ({})", n, n.getC().getClass().getSimpleName());

        getDefaultDataFlowInputTemplates(n)
                .forEach((fieldName, template) -> {
                    try {
                        typeTemplate(template);
                    } catch (TemplateException e) {
                        log.error("{} type error for field '{}': {}", n, fieldName, e.getMessage());
                        throw e;
                    }
                        }
                );
    }

    private void addNodeInfo(NodeContainer<?> n, ControlFlowGraph cfg, ScrapeInstance spec, Set<NodeContainer<?>> visited) {
        log.debug("=== Adding node info {}", n);

        getDefaultDataFlowOutput(n).forEach((fieldName, output) -> {
            try {
                Type tt = new ReplaceCapturesOrCrashVisitor(captures).visit(output.get());
                if (!tt.equals(output.get())) {
                    log.debug("Captured types {} ==> {}", output, tt);
                    Term<String> loc = output.getLocation();
                    output = new L<>(tt){};
                    output.setLocation(loc);
                }

                {
                    log.debug("Adding location data {} :: {}", output.getLocation(), output);
                    env.add(output.getLocation(), output.get());
                }
            } catch (TemplateException e){
                log.error("{} type error for field '{}': {}", n, fieldName, e.getMessage());
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
                Method method = control.getDeclaredMethod("infoAfter", TypeChecker.class,
                        NodeContainer.class,
                        ControlFlowGraph.class,
                        ScrapeInstance.class,
                        Set.class);
                method.invoke(null, this, n, cfg, spec, visited);
            } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException ignored) {
//                System.out.println("[Skip] Could not find control for " + nodeClass + ": " + controlClass);
            } catch (InvocationTargetException e) {
                if(e.getCause() instanceof TemplateException) {
                    log.error("{} type error for field: {}", n, e.getCause().getMessage());
                    throw (TemplateException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        }
    }

    // for nodes that do special things
//    private void addNodeInfoSpecial(TypeEnvironment env, NodeContainer<?> n, ScrapeInstance inst) {
//
//        String nodeName = n.getC().getClass().getSimpleName();
//        String controlClass = "scraper.plugins.core.typechecker.data."+nodeName+"Data";
//        try {
//            Class<?> control = Class.forName(controlClass);
//            Method method = control.getDeclaredMethod("infoBefore", TypeEnvironment.class, NodeContainer.class, ScrapeInstance.class);
//            method.invoke(null, env, n, inst);
//        } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException ignored) {
////                System.out.println("[Skip] Could not find control for " + nodeClass + ": " + controlClass);
//        } catch (InvocationTargetException e) {
//            throw new RuntimeException(e);
//        }
//    }


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

    public void add(Term<?> known) {
        Type tt = new ReplaceCapturesOrCrashVisitor(captures).visit(known.getToken().get());
        env.add(known, tt);
    }
}
