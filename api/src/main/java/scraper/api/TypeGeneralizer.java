package scraper.api.template;

import scraper.api.TemplateException;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Visitor class for a Java Type to descend into and visit generic types.
 * One Type is visited at most once to avoid infinite recursion by recursive type bounds.
 * Captures type variables of the target type and saves it for other mentions of the type variable again.
 * Returns true, if the target type is more specialized than the known type,
 * e.g. String is more specialized than ?
 */
public abstract class TypeGeneralizer {
    private final Map<String, Type> capturedTypes;
    public Map<String, T<?>> newCaptures = new HashMap<>();
    public Exception error;


    public TypeGeneralizer(Map<String, T<?>> capturedTypes) {
        this.capturedTypes = capturedTypes.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().get()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public final Type visit(Type knownType, Type targetType) {

        if (knownType instanceof TypeVariable) {
            return handleTypeVar((TypeVariable<?>) knownType, targetType);
        } else if (knownType instanceof WildcardType || targetType instanceof WildcardType) {
            throw new TemplateException("Wildcards not allowed");
        } else if (knownType instanceof ParameterizedType) {
            return (visitParameterizedType((ParameterizedType) knownType, targetType));
        } else if (knownType instanceof Class) {
            return handleClass((Class<?>) knownType, targetType);
        } else if (knownType instanceof GenericArrayType) {
            throw new TemplateException("Array type not allowed");
        } else {
            throw new AssertionError("Bad type ");
        }
    }

    private Type handleTypeVar(TypeVariable<?> knownType, Type targetType) {
        if(targetType == Object.class) {
            Type capt = capturedTypes.get(knownType.getTypeName());
            if(capt == null) {
                newCaptures.put(knownType.getTypeName(), new T<>(knownType){});
                return knownType;
            } else {
                return capt;

            }
        }

        if(capturedTypes.containsKey(knownType.getTypeName())) {
            return visit(capturedTypes.get(knownType.getTypeName()), targetType);
        }

        return visit(Object.class, targetType);
    }

    private Type handleClass(Class<?> knownType, Type targetType) {
        if(targetType == Object.class) return knownType;


        if(targetType instanceof TypeVariable) {
            String unboxedTypeName = (targetType.getTypeName().contains("$")
                    ? targetType.getTypeName().substring(0, targetType.getTypeName().indexOf("$"))
                    : targetType.getTypeName()
            );
//            Type maybeCaptured = capturedTypes.get(targetType.getTypeName());
            Type maybeCaptured = capturedTypes.get(unboxedTypeName);
            if(maybeCaptured == null) {
                newCaptures.put(targetType.getTypeName(), new T<>(knownType){});
                return knownType;
            } else {
                return visit(knownType, maybeCaptured);
            }
        }

        if(knownType == Object.class) return targetType;
        if(knownType == targetType) return knownType;


        error = new TemplateException("Could not match known type " + knownType.getSimpleName() + " with " + targetType.getTypeName());
        return null;
    }

    private Type visitParameterizedType(ParameterizedType t, Type target) {
        if(target == Object.class) return t;
        if(target instanceof TypeVariable) {
            if(capturedTypes.containsKey(target.getTypeName())) {
                return visit(t, capturedTypes.get(target.getTypeName()));
            } else {
                newCaptures.put(target.getTypeName(), new T<>(t){});
                return t;
            }
        }

        if(target instanceof ParameterizedType) {
            return visitParameterizedTypeBoth(t, (ParameterizedType) target);
        }

        return null;
    }

    private Type visitParameterizedTypeBoth(ParameterizedType t, ParameterizedType target) {
        if (t.getActualTypeArguments().length != target.getActualTypeArguments().length)
            throw new TemplateException("Bad parameterized type length");

        Type[] parameters = new Type[t.getActualTypeArguments().length];

        for (int i = 0; i < t.getActualTypeArguments().length; i++) {

            Type t1 = t.getActualTypeArguments()[i];
            Type t2 = target.getActualTypeArguments()[i];

            Type replace = visit(t1, t2);
            if(replace == null) return null;

            parameters[i] = replace;
        }

        return new ParameterizedType() {
            @Override public Type[] getActualTypeArguments() { return parameters; }
            @Override public Type getRawType() { return t.getRawType(); }
            @Override public Type getOwnerType() { return t.getOwnerType(); }
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
                String arr = Arrays.stream(getActualTypeArguments())
                        .map(Type::getTypeName)
                        .collect(Collectors.joining(", "));
                return getRawType().getTypeName()+"<"+arr+">";
            }
        };
    }

}
