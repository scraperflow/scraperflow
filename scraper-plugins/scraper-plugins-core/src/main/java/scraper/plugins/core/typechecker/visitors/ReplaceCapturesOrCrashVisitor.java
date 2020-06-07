package scraper.plugins.core.typechecker.visitors;

import scraper.api.exceptions.TemplateException;
import scraper.api.template.T;

import java.lang.reflect.*;
import java.util.*;

/**
 * Visitor class for a Java Type to descend into and visit generic types.
 * Replaces type variables with wildcards.
 */
public class ReplaceCapturesOrCrashVisitor {
    private final Set<Type> visited = new HashSet<>();
    private final Map<String, T<?>> captures;

    public ReplaceCapturesOrCrashVisitor(Map<String, T<?>> captures) {
        this.captures = captures;
    }

    public final Type visit(Type knownType) {
        return visit(new Type[]{knownType})[0];
    }

    private Type[] visit(Type[] knownTypes) {
        for (int i = 0; i < knownTypes.length; i++) {
            Type known = knownTypes[i];

            if (known == null || !visited.add(known)) return knownTypes;

            if (known instanceof TypeVariable) {
                knownTypes[i] = visitTypeVariable((TypeVariable<?>) known);
            } else if (known instanceof WildcardType) {
                knownTypes[i] = visitWildcardType((WildcardType) known);
            } else if (known instanceof ParameterizedType) {
                knownTypes[i] = visitParameterizedType((ParameterizedType) known);
            } else if (known instanceof Class) {
                knownTypes[i] = visitClass((Class<?>) known);
            } else if (known instanceof GenericArrayType) {
                knownTypes[i] = visitGenericArrayType((GenericArrayType) known);
            } else {
                throw new AssertionError("Bad type " + known);
            }
        }

        return knownTypes;
    }

    // keep raw classes and wildcards
    private Type visitClass(Class<?> t) { return t; }
    private Type visitWildcardType(WildcardType t) { return t; }

    private Type visitGenericArrayType(GenericArrayType t) {
        Type component = visit(t.getGenericComponentType());
        return (GenericArrayType) () -> component;
    }

    private Type visitParameterizedType(ParameterizedType t) {
        Type owner = visit(t.getOwnerType());
        Type[] parameters = visit(t.getActualTypeArguments());

        return new ParameterizedType() {
            @Override public Type[] getActualTypeArguments() { return parameters; }
            @Override public Type getRawType() { return t.getRawType(); }
            @Override public Type getOwnerType() { return owner; }
            @Override public boolean equals(Object o) {
                if (o instanceof ParameterizedType) {
                    // Check that information is equivalent
                    ParameterizedType that = (ParameterizedType) o;
                    if (this == that) return true;

                    Type thatOwner   = that.getOwnerType();
                    Type thatRawType = that.getRawType();

                    return
                            Objects.equals(getOwnerType(), thatOwner) &&
                                    Objects.equals(getRawType(), thatRawType) &&
                                    Arrays.equals(getActualTypeArguments(),
                                            that.getActualTypeArguments());
                } else return false;
            }

            @Override
            public int hashCode() {
                return Arrays.hashCode(getActualTypeArguments()) ^
                        Objects.hashCode(getOwnerType()) ^
                        Objects.hashCode(getRawType());
            }
            @Override
            public String toString() {
                String arr = Arrays.toString(getActualTypeArguments());
                return getRawType().getTypeName()+"<"+arr.substring(1, arr.length()-1)+">";
            }
        };
    }

    private Type visitTypeVariable(@SuppressWarnings("unused") TypeVariable<?> t) {
        T<?> captured = captures.get(t.getName());
        if(captured == null)  return t;
//            throw new TemplateException("Type variable not captured and missing for input! " + t);
        return captured.get();
    }

}
