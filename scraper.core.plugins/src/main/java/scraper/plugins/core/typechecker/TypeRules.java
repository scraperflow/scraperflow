package scraper.plugins.core.typechecker;

import scraper.api.*;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.util.TemplateUtil;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.DEBUG;

public class TypeRules extends DefaultVisitor<Object> {

    private static final System.Logger log = System.getLogger("TypeRules");

    private final TypeChecker checker;
    private final TypeEnvironment env;

    public TypeRules (TypeEnvironment env, TypeChecker checker) {
        this.env = env;
        this.checker = checker;
    }

    /*
     * =====================
     * Template Typing Rules
     * =====================
     */

    // T-Template
    public T<?> typeTemplate(T<?> t) {
        if(t.getTerm() != null) return (T<?>) t.getTerm().accept(this);
        else {
            // This is a corner case for optional templates, carry on
            return t;
        }
    }

    // T-Constant-Template
    @Override public T<?> visitPrimitive(Primitive<?> primitive) {
        if(
                !primitive.isTypeVariable() &&
                        !primitive.getToken().equalsType(new T<String>(){})
                        && !primitive.getToken().equalsType(new T<Integer>(){})
                        && !primitive.getToken().equalsType(new T<Object>(){})
                        && !primitive.getToken().equalsType(new T<Address>(){})
        ) {
            log.log(System.Logger.Level.ERROR,"Not a primitive: {0}", primitive);
            throw new TemplateException("Not an allowed primitive: " + primitive);
        }
        String capt = primitive.getToken().getTypeString();
        log.log(DEBUG, " ====== T-Constant-Template: Checking primitive {0} :: {1}", primitive, capt);

        // eval is also type check, but not needed
        Object evalObj = primitive.eval();

        T<?> inferredToken = FlowMapImpl.inferTypeToken(evalObj);

        if(primitive.isTypeVariable()) {
            env.putIfNotConflicting(capt, inferredToken);
        }

        return inferredToken;
    }

    // T-Key-Lookup-Template
    @Override public T<?> visitFlowKeyLookup(FlowKeyLookup<?> mapKey) {
        log.log(DEBUG, " ====== T-Key-Lookup-Template: Checking keylookup {0} with target type {1}", mapKey, mapKey.getToken().getTypeString());
        { // (1) check string type
//            log.log(DEBUG, " === T-Key-Lookup-Template (1): check inner template {0} is a String", mapKey.getKeyLookup());
            T<?> innerType = (T<?>) mapKey.getKeyLookup().accept(this);
            if(!innerType.equalsType(new T<String>(){}))
                throw new TemplateException("Inner type of flow key lookup is not a string, instead "+innerType.getTypeString());
        }

        { // (2) check if inner template is in env
            log.log(DEBUG, " === T-Key-Lookup-Template (2): check if inner template {0} is in environment with correct target type", mapKey.getKeyLookup());
            Term<String> keyTemplate = mapKey.getKeyLookup();

            T<?> known = env.get(keyTemplate);
            if(known == null)
                throw new TemplateException("Template term for key lookup not in environment: " + keyTemplate);


            if(mapKey.isTypeVariable()) {
                String capt = mapKey.getToken().getTypeString();

                // check if capture matches
                if(env.resolve(capt) != null) {
                    if(!env.resolve(capt).equalsType(known)) {
                        throw new TemplateException("Captured " + capt + " with different type already: "
                                + env.resolve(capt) + " != " + known.getTypeString());
                    }
                }

                return env.putIfNotConflicting(capt, known);
            } else
            if(known.get() instanceof TypeVariable) {
                log.log(DEBUG, "Specializing {0} :: {1}", known.getTypeString(), mapKey.getToken().getTypeString());
                env.putIfNotConflicting(known.getTypeString(), mapKey.getToken());
                env.addSpecialize(keyTemplate, mapKey.getToken());
                return mapKey.getToken();
            } else {

                Type target = mapKey.getToken().get();
                TypeGeneralizer lizer = new TypeGeneralizer(env.captures){};
                Type newType = lizer.visit(known.get(), target);
                if (newType == null) {
                    if(lizer.error != null) throw new TemplateException(lizer.error, "Template term for key lookup bad type: " + known.getTypeString() + " != " + mapKey.getToken().getTypeString());
                    else throw new TemplateException("Template term for key lookup bad type: " + known.getTypeString() + " != " + mapKey.getToken().getTypeString());
                } else {
                    env.captures.putAll(lizer.newCaptures);
                    env.addSpecialize(mapKey.getKeyLookup(), new T<>(newType){});
                    return new T<>(newType){};
                }
            }
        }
    }

    // T-List-Template   [t1:T , ... , tn:T] :: List<T>
    @Override public T<?> visitListTerm(ListTerm<?> list) {
        log.log(DEBUG, " ====== T-List-Template: Checking list {0} with target type {1}", list.getTerms(), list.getToken().getTypeString());

        // EMTPY LIST
        if(list.getTerms().isEmpty()) {
            T<List<Object>> newToken = TemplateUtil.listOf(new T<>(){});
            env.putIfNotConflicting(list.getToken().getTypeString(), newToken);
            env.putIfNotConflicting(list.getElementType().getTypeString(), new T<>(){});
            return newToken;
        }

        List<T<?>> allTokens =  (list.getTerms().stream()
                .map(t -> (T<?>) t.accept(this))
                .distinct()
                .collect(Collectors.toList()));

        // OBJECT TARGET
        if(list.getElementType().get() == Object.class) return list.getToken();

        // get fixpoint of all tokens
        T<?> mostPreciseInnerType = env.fixpoint(allTokens);

        // if list term is type variable
        // then capture A' => List<A>
        if(list.isTypeVariable()) {
            env.putIfNotConflicting(list.getToken().getTypeString(), TemplateUtil.listOf(mostPreciseInnerType));
            env.putIfNotConflicting(list.getElementType().getTypeString(), mostPreciseInnerType);
            return TemplateUtil.listOf(mostPreciseInnerType);
        }

        // if list term is type
        // then check result inner type with target inner type
        else {
            if(mostPreciseInnerType.equalsType(list.getElementType())) {
                return TemplateUtil.listOf(mostPreciseInnerType);
            } else {
                if(list.getElementType().equalsType(new T<Object>(){})) {
                    // ignore, ANY type allowed
                    // TODO what to return?
                    return TemplateUtil.listOf(mostPreciseInnerType);
                } else {
                    throw new TemplateException("List term type variable resolves to different type than expected: " + mostPreciseInnerType + " != " + list.getElementType());
                }
            }
        }
    }

    // T-Map-Template {k1 => t1:T, ... , kn => tn:T} :: Map<String, T>
    @Override public T<?> visitMapTerm(MapTerm<?> mapTerm) {
        log.log(DEBUG, " ====== T-Map-Template: Checking map {0} with target type {1}", mapTerm.getTerms().keySet(), mapTerm.getToken().getTypeString());
        log.log(DEBUG, " ====== element type {0}", mapTerm.getElementType().getTypeString());
        // EMPTY MAP
        if(mapTerm.getTerms().isEmpty()) {
            T<Map<String, Object>> newToken = TemplateUtil.mapOf(new T<>(){}, new T<>(){});
            env.putIfNotConflicting(mapTerm.getToken().getTypeString(), newToken);
            env.putIfNotConflicting(mapTerm.getElementType().getTypeString(), new T<>(){});
            return newToken;
        }

        List<T<?>> allTokens =  (mapTerm.getTerms().values().stream()
                .map(term -> (T<?>) term.accept(this))
                .distinct()
                .collect(Collectors.toList()));

        // OBJECT TARGET
        if(mapTerm.getElementType().get() == Object.class) return mapTerm.getToken();

        // get fixpoint of all tokens
        T<?> mostPreciseInnerType = env.fixpoint(allTokens);

        // if map term is type variable
        // then capture A => Map<String, A$MapOf>
        if(mapTerm.isTypeVariable()) {
            env.putIfNotConflicting(mapTerm.getToken().getTypeString(), TemplateUtil.mapOf(new T<String>(){}, mostPreciseInnerType));
            // TODO why is this not necessary again ?
//            checker.putIfNotConflicting(mapTerm.getElementType().getTypeString(), mostPreciseInnerType);
            return TemplateUtil.mapOf(new T<String>(){}, mostPreciseInnerType);
        }

        // if map term is type
        // then check result inner type with target inner type
        else {
            if(mostPreciseInnerType.equalsType(mapTerm.getElementType())) {
                return TemplateUtil.mapOf(new T<String>(){}, mostPreciseInnerType);
            } else {
                if(mapTerm.getElementType().equalsType(new T<Object>(){})) {
                    // ignore, ANY type allowed
                    // TODO what to return?
                    return TemplateUtil.mapOf(new T<String>(){}, mostPreciseInnerType);
                } else {
                    throw new TemplateException("Map term type variable resolves to different type than expected: " + mostPreciseInnerType + " != " + mapTerm.getElementType());
                }
            }
        }
    }

    // T-Map-Lookup-Template
    @Override public T<?> visitMapLookup(MapLookup<?> mapLookup) {
        mapLookup.getMapObjectTerm().accept(this);
        mapLookup.getKeyTerm().accept(this);
        return mapLookup.getToken();
    }

    // T-List-Lookup
    @Override public T<?> visitListLookup(ListLookup<?> listLookup) {
        listLookup.getIndexTerm().accept(this);
        listLookup.getListObjectTerm().accept(this);
        return listLookup.getToken();
    }

    // T-Concat-Lookup
    @Override public T<?> visitConcatenation(Concatenation concat) {
        concat.getConcatenationTerms().forEach(e -> e.accept(this));
        return concat.getToken();
    }

}
