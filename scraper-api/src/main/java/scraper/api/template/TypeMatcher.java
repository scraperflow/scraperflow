package scraper.api.template;

import scraper.api.exceptions.TemplateException;
import scraper.api.node.Address;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Visitor class for a Java Type to descend into and visit generic types.
 * One Type is visited at most once to avoid infinite recursion by recursive type bounds.
 * Captures type variables of the target type and saves it for other mentions of the type variable again.
 * Returns true, if the target type is more specialized than the known type,
 * e.g. String is more specialized than ?
 */
public abstract class TypeMatcher {
    private final Set<Type> visited = new HashSet<>();
    private final Map<String, Type> capturedTypes = new HashMap<>();

    public final boolean visit(Type knownType, Type targetType) {
        return visit(new Type[]{knownType}, new Type[]{targetType});
    }

    public final Map<String, T<?>> visitAndReturnCaptures(Map<String, T<?>> captures, Type knownType, Type targetType) {
        for (String typevariable : captures.keySet()) {
            capturedTypes.put(typevariable, captures.get(typevariable).get());
        }

        visit(new Type[]{knownType}, new Type[]{targetType});
        return capturedTypes.entrySet().stream()
                .map(
                        e -> Map.entry(e.getKey(), new T<>(e.getValue()){})
                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public final Map<String, T<?>> visitAndReturnCaptures(Type knownType, Type targetType) {
        visit(new Type[]{knownType}, new Type[]{targetType});
        return capturedTypes.entrySet().stream()
                .map(
                        e -> Map.entry(e.getKey(), new T<>(e.getValue()){})
                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Visits the given types in order.
     */
    private boolean visit(Type[] knownTypes, Type[] targetTypes) {
        if (knownTypes.length != targetTypes.length) throw new TemplateException("Bad type length");

        for (int i = 0; i < knownTypes.length; i++) {
            Type known = knownTypes[i];
            Type target = targetTypes[i];

            if (known == null) return false;

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
                    capturedTypes.put(name, t);
                    return false;
                } else {
                    if(t != captured) {
                        if(captured instanceof TypeVariable) {
                            capturedTypes.put(name, t);

                            return false;
                        } else {
                            throw new TemplateException("Captured type mismatch: " + t + " != " + captured + " (~> "+name+")");
                        }
                    } else {
                        return false;
                    }
                }
            } else if (target instanceof WildcardType) {
//                log.trace("Target is less specialized than known type");
                return false;
            }
        }

        if(t == Object.class) return false;
        if(target == Object.class) return false;

//        // check Number -> String
//        if(t == Integer.class && target == String.class) return false;
//
//        // check Boolean -> String
//        if(t == Boolean.class && target == String.class) return false;

        if(
                Address.class.isAssignableFrom(t) && Address.class.isAssignableFrom((Class) target)
        ) {
            return false;
        }


        throw new TemplateException("Types do not match: " + t + " != " + target);
    }

    private boolean visitGenericArrayType(GenericArrayType t, Type target) {
        // crash if target is not a GenericArrayType
        return visit(t.getGenericComponentType(), ((GenericArrayType) target).getGenericComponentType());
    }

    private boolean visitParameterizedType(ParameterizedType t, Type target) {
        if (target instanceof ParameterizedType) {
            visit(t.getRawType(), ((ParameterizedType) target).getRawType());
//            log.trace("Target type is also parameterized");
            boolean a = visit(t.getOwnerType(), ((ParameterizedType) target).getOwnerType());
            boolean b = visit(t.getActualTypeArguments(), ((ParameterizedType) target).getActualTypeArguments());
            return a || b;
        } else if(target instanceof TypeVariable) {
            Type knownCapture = capturedTypes.get(((TypeVariable) target).getName());
            if(knownCapture != null && !knownCapture.equals(target)) {
                if(target instanceof TypeVariable) {
//                    log.debug("Capturing {} -> {}", target, t);
                    capturedTypes.put(target.getTypeName(), t);

                    return false;
                } else {
                    throw new TemplateException("Types do not match: " + knownCapture + " != " + target);
                }
            }

            capturedTypes.put(((TypeVariable<?>) target).getName(), t);
//            log.trace("Capture {} -> {}", target.getTypeName(), t);
            return false;
        } else if(target == Object.class) {
//            log.trace("Target is most generic: {} << {}", target, t);
            return false;
        } else {
//            log.error("Types do not match: '{}' != '{}'", t, target);
            throw new TemplateException("Types do not match: " + t + " != " + target);
        }
    }

    @SuppressWarnings("unused") // ignored
    private boolean visitTypeVariable(TypeVariable<?> t, Type target) {
        // capture
        Type knownCapture = capturedTypes.get(t.getTypeName());
        if(knownCapture != null && !knownCapture.equals(target)) {
            if(target instanceof TypeVariable) {
//                log.debug("Capturing {} -> {}", target, t);
                capturedTypes.put(target.getTypeName(), t);

                return false;
            } else {
                throw new TemplateException("Types do not match: " + knownCapture + " != " + target);
            }
        }
        capturedTypes.put(t.getName(), target);
        return false;
    }

    private boolean visitWildcardType(WildcardType t, Type target) {
        if(!t.getTypeName().equalsIgnoreCase("?")) {
//            log.error("Inferred known type should never have lower or upper bounds: '{}'. " +
//                    "Ignoring, fix infer type implementation", t);
            return false;
        }
        if(target instanceof WildcardType) return false;
        if(target instanceof TypeVariable) return false;
//        log.trace("Target is more specialized than current type '{}' ~> '{}'", t, target);
        return true;
    }

}
