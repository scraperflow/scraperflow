package scraper.plugins.core.typechecker;

import com.fasterxml.jackson.databind.ser.std.StdArraySerializers;
import org.slf4j.Logger;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.Address;
import scraper.api.template.*;
import scraper.plugins.core.typechecker.visitors.ReplaceCapturesOrCrashVisitor;
import scraper.util.TemplateUtil;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static scraper.api.template.T.*;

public class TypeRules extends DefaultVisitor<Object> {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger("TypeRules");

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
            log.error("Not a primitive: {}", primitive);
            throw new TemplateException("Not an allowed primitive: " + primitive);
        }
        String capt = primitive.getToken().getTypeString();
        log.debug(" ====== T-Constant-Template: Checking primitive {} :: {}", primitive, capt);

        // eval is also type check, but not needed
        Object evalObj = primitive.eval();

        T<?> inferredToken = FlowMapImpl.inferTypeToken(evalObj);

        if(primitive.isTypeVariable()) {
            checker.putIfNotConflicting(capt, inferredToken);
        }

        return inferredToken;
    }

    // T-Key-Lookup-Template
    @Override public T<?> visitFlowKeyLookup(FlowKeyLookup<?> mapKey) {
        log.debug(" ====== T-Key-Lookup-Template: Checking keylookup {} with target type {}", mapKey, mapKey.getToken().getTypeString());
        { // (1) check string type
            log.debug(" === T-Key-Lookup-Template (1): check inner template '{}' is a String", mapKey.getKeyLookup());
            T<?> innerType = (T<?>) mapKey.getKeyLookup().accept(this);
            if(!innerType.equalsType(new T<String>(){}))
                throw new TemplateException("Inner type of flow key lookup is not a string, instead "+innerType.getTypeString());
        }

        { // (2) check if inner template is in env
            log.debug(" === T-Key-Lookup-Template (2): check if inner template '{}' is in environment with correct target type", mapKey.getKeyLookup());
            Term<String> keyTemplate = mapKey.getKeyLookup();

            T<?> known = env.get(keyTemplate);
            if(known == null)
                throw new TemplateException("Template term for key lookup not in environment: " + keyTemplate);


            if(mapKey.isTypeVariable()) {
                String capt = mapKey.getToken().getTypeString();

                // check if capture matches
                if(checker.resolve(capt) != null) {
                    if(!checker.resolve(capt).equalsType(known)) {
                        throw new TemplateException("Captured " + capt + " with different type already: "
                                + checker.resolve(capt) + " != " + known.getTypeString());
                    }
                }

                return checker.putIfNotConflicting(capt, known);
            } else
            if(known.get() instanceof TypeVariable) {
                log.debug("Specializing {} :: {}", known.getTypeString(), mapKey.getToken().getTypeString());
                checker.putIfNotConflicting(known.getTypeString(), mapKey.getToken());
                env.add(keyTemplate, mapKey.getToken());
                return mapKey.getToken();
            } else {

                Type target = mapKey.getToken().get();

                TypeGeneralizer lizer = new TypeGeneralizer(checker.captures){};

                Type newType = lizer.visit(known.get(), target);
                if (newType == null) {
                    throw new TemplateException("Template term for key lookup bad type: " + known.getTypeString() + " != " + mapKey.getToken().getTypeString());
                } else {
                    checker.captures.putAll(lizer.newCaptures);
                    env.add(mapKey.getKeyLookup(), new T<>(newType){});
                    return new T<>(newType){};
                }
            }
        }
    }

    // T-List-Template   [t1:T , ... , tn:T] :: List<T>
    @Override public T<?> visitListTerm(ListTerm<?> list) {
        log.debug(" ====== T-List-Template: Checking list {} with target type {}", list.getTerms(), list.getToken().getTypeString());

        // EMTPY LIST
        if(list.getTerms().isEmpty()) {
            T<List<Object>> newToken = TemplateUtil.listOf(new T<>(){});
            checker.putIfNotConflicting(list.getToken().getTypeString(), newToken);
            checker.putIfNotConflicting(list.getElementType().getTypeString(), new T<>(){});
            return newToken;
        }

        List<T<?>> allTokens =  (list.getTerms().stream()
                .map(t -> (T<?>) t.accept(this))
                .distinct()
                .collect(Collectors.toList()));

        // OBJECT TARGET
        if(list.getElementType().get() == Object.class) return list.getToken();

        // get fixpoint of all tokens
        T<?> mostPreciseInnerType = checker.fixpoint(allTokens);

        // if list term is type variable
        // then capture A' => List<A>
        if(list.isTypeVariable()) {
            checker.putIfNotConflicting(list.getToken().getTypeString(), TemplateUtil.listOf(mostPreciseInnerType));
            checker.putIfNotConflicting(list.getElementType().getTypeString(), mostPreciseInnerType);
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
        log.debug(" ====== T-Map-Template: Checking map {} with target type {}", mapTerm.getTerms().keySet(), mapTerm.getToken().getTypeString());

        // EMPTY MAP
        if(mapTerm.getTerms().isEmpty()) {
            T<Map<String, Object>> newToken = TemplateUtil.mapOf(new T<>(){}, new T<>(){});
            checker.putIfNotConflicting(mapTerm.getToken().getTypeString(), newToken);
            checker.putIfNotConflicting(mapTerm.getElementType().getTypeString(), new T<>(){});
            return newToken;
        }

        List<T<?>> allTokens =  (mapTerm.getTerms().values().stream()
                .map(term -> (T<?>) term.accept(this))
                .distinct()
                .collect(Collectors.toList()));

        // OBJECT TARGET
        if(mapTerm.getElementType().get() == Object.class) return mapTerm.getToken();

        // get fixpoint of all tokens
        T<?> mostPreciseInnerType = checker.fixpoint(allTokens);

        // if map term is type variable
        // then capture A => Map<String, A$MapOf>
        if(mapTerm.isTypeVariable()) {
            checker.putIfNotConflicting(mapTerm.getToken().getTypeString(), TemplateUtil.mapOf(new T<String>(){}, mostPreciseInnerType));
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
