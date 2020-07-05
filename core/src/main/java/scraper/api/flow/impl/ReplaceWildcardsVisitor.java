package scraper.api.flow.impl;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Visitor class for a Java Type to descend into and visit generic types.
 * Replaces type variables with wildcards.
 */
abstract class ReplaceWildcardsVisitor {
    private final Set<Type> visited = new HashSet<>();

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
        };
    }

    private Type visitTypeVariable(@SuppressWarnings("unused") TypeVariable<?> t) {
//        System.out.println("Replacing type variable " + t.getName() + " with wildcard ?");
        return new WildcardType() {
            @Override public Type[] getUpperBounds() { return new Type[]{Object.class}; }
            @Override public Type[] getLowerBounds() { return new Type[0]; }
            @Override
            public boolean equals(Object o) {
                if (o instanceof WildcardType) {
                    WildcardType that = (WildcardType) o;
                    return
                            Arrays.equals(this.getLowerBounds(), that.getLowerBounds()) &&
                                    Arrays.equals(this.getUpperBounds(), that.getUpperBounds());
                } else return false;
            }

            @Override
            public int hashCode() { return Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds()); }
        };
    }

}
