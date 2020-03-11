package scraper.api.flow.impl;

import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import scraper.api.exceptions.TemplateException;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Visitor class for a Java Type to descend into and visit generic types.
 * One Type is visited at most once to avoid infinite recursion by recursive type bounds.
 * Captures type variables of the target type and saves it for other mentions of the type variable again.
 * Returns true, if the target type is more specialized than the known type,
 * e.g. String is more specialized than ?
 */
abstract class GenericTypeMatcher {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger("TypeDescent");

    private final Set<Type> visited = new HashSet<>();
    private final Map<String, Type> capturedTypes = new HashMap<>();

    public final boolean visit(Type knownType, Type targetType) {
        return visit(new Type[]{knownType}, new Type[]{targetType});
    }

    /**
     * Visits the given types in order.
     */
    private boolean visit(Type[] knownTypes, Type[] targetTypes) {
        if (knownTypes.length != targetTypes.length) throw new TemplateException("Bad type length");

        for (int i = 0; i < knownTypes.length; i++) {
            Type known = knownTypes[i];
            Type target = targetTypes[i];

            if (known == null || !visited.add(known)) return false;

            if (known instanceof TypeVariable) {
                if(visitTypeVariable((TypeVariable<?>) known, target)) return true;
            } else if (known instanceof WildcardType) {
                if(visitWildcardType((WildcardType) known, target)) return true;
            } else if (known instanceof ParameterizedType) {
                if(visitParameterizedType((ParameterizedType) known, target)) return true;
            } else if (known instanceof Class) {
                if(visitClass((Class<?>) known, target)) return true;
            } else if (known instanceof GenericArrayType) {
                if(visitGenericArrayType((GenericArrayType) known, target)) return true;
            } else {
                throw new AssertionError("Bad type " + known);
            }
        }

        return false;
    }

    private boolean visitClass(Class<?> t, Type target) {
        if (target instanceof Class<?>) {
            if (t == target) {
                return false;
            }
        } else {
            if(target instanceof TypeVariable<?>) {
                String name = ((TypeVariable) target).getName();
                Type captured = capturedTypes.get(name);
                if(captured == null) {
                    log.trace("Capture {} -> {}", name, t);
                    capturedTypes.put(name, t);
                    return false;
                } else {
                    if(t != captured) {
                        throw new TemplateException("Captured type mismatch: " + t + " != " + captured + " (~> "+name+")");
                    } else {
                        return false;
                    }
                }
            } else if (target instanceof WildcardType) {
                log.trace("Target is less specialized than known type");
                return false;
            }
        }

        // last resort: sub relation
        TypeToken<?> targetToken = TypeToken.of(target);
        TypeToken<?> knownToken  = TypeToken.of(t);
        if(knownToken.isSubtypeOf(targetToken)) {
            log.debug("known {} is subtype of {}", knownToken, targetToken);
            return false;
        }

        log.error("Types do not match: '{}' != '{}'", t, target);
        throw new TemplateException("Types do not match: " + t + " != " + target);
    }

    private boolean visitGenericArrayType(GenericArrayType t, Type target) {
        // crash if target is not a GenericArrayType
        return visit(t.getGenericComponentType(), ((GenericArrayType) target).getGenericComponentType());
    }

    private boolean visitParameterizedType(ParameterizedType t, Type target) {
        if (target instanceof ParameterizedType) {
            visit(t.getRawType(), ((ParameterizedType) target).getRawType());
            log.trace("Target type is also parameterized");
            boolean a = visit(t.getOwnerType(), ((ParameterizedType) target).getOwnerType());
            boolean b = visit(t.getActualTypeArguments(), ((ParameterizedType) target).getActualTypeArguments());
            return a || b;
        } else if(target instanceof TypeVariable) {
            log.trace("Capture {} -> {}", target.getTypeName(), t);
            return false;
        } else if(target == Object.class) {
            log.trace("Target is most generic: {} << {}", target, t);
            return false;
        } else {
            log.error("Types do not match: '{}' != '{}'", t, target);
            throw new TemplateException("Types do not match: " + t + " != " + target);
        }
    }

    @SuppressWarnings("unused") // ignored
    private boolean visitTypeVariable(TypeVariable<?> t, Type target) {
        log.error("Inferred known type should never have type variables. " +
                "Ignoring, fix infer type implementation: {}", t);
        throw new TemplateException("Known type has a type variable");
    }

    private boolean visitWildcardType(WildcardType t, Type target) {
        if(!t.getTypeName().equalsIgnoreCase("?")) {
            log.error("Inferred known type should never have lower or upper bounds: '{}'. " +
                    "Ignoring, fix infer type implementation", t);
            return false;
        }
        if(target instanceof WildcardType) return false;
        if(target instanceof TypeVariable) return false;
        log.trace("Target is more specialized than current type '{}' ~> '{}'", t, target);
        return true;
    }
}
