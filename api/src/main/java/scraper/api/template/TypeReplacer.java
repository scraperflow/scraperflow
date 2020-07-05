package scraper.api.template;

import scraper.api.exceptions.TemplateException;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Visitor class for a Java Type to descend into and visit generic types.
 * One Type is visited at most once to avoid infinite recursion by recursive type bounds.
 * Captures type variables of the target type and saves it for other mentions of the type variable again.
 * Returns true, if the target type is more specialized than the known type,
 * e.g. String is more specialized than ?
 */
@SuppressWarnings({"UnnecessaryLocalVariable", "unchecked", "TypeParameterHidesVisibleType"})
public abstract class TypeReplacer {

    private final String replace;

    public TypeReplacer(String replaceWith) {
        this.replace = replaceWith;
    }

    public final Type visit(Type knownType){

        if (knownType instanceof TypeVariable) {
            return handleTypeVar((TypeVariable<?>) knownType);
        } else if (knownType instanceof WildcardType) {
            throw new TemplateException("Wildcards not allowed");
        } else if (knownType instanceof ParameterizedType) {
            return (visitParameterizedType((ParameterizedType) knownType));
        } else if (knownType instanceof Class) {
            return handleClass((Class<?>) knownType);
        } else if (knownType instanceof GenericArrayType) {
            throw new TemplateException("Array type not allowed");
        } else {
            throw new AssertionError("Bad type ");
        }
    }

    private <X extends GenericDeclaration> Type handleTypeVar(TypeVariable<?> knownType) {
        TypeVariable<X> realReplaced = new TypeVariable<>() {
            @Override
            public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
                return knownType.getAnnotation(annotationClass);
            }

            @Override
            public Annotation[] getAnnotations() {
                return knownType.getAnnotations();
            }

            @Override
            public Annotation[] getDeclaredAnnotations() {
                return knownType.getDeclaredAnnotations();
            }

            @Override
            public Type[] getBounds() {
                return knownType.getBounds();
            }

            @Override
            public X getGenericDeclaration() {
                return (X) knownType.getGenericDeclaration();
            }

            @Override
            public String getName() {
                return knownType.getTypeName() + "$" +replace;
            }

            @Override
            public AnnotatedType[] getAnnotatedBounds() {
                throw new AssertionError();
            }

            @Override
            public String getTypeName() {
                return knownType.getTypeName() + "$" +replace;
            }

            @Override
            public String toString() {
                return knownType.getTypeName() + "$" +replace;
            }
        };

        return realReplaced;
    }

    private Type handleClass(Class<?> knownType) {
        return knownType;
    }

    private Type visitParameterizedType(ParameterizedType t) {
        return visitParameterizedTypeBoth(t);
    }

    private Type visitParameterizedTypeBoth(ParameterizedType t) {
        Type[] parameters = new Type[t.getActualTypeArguments().length];

        for (int i = 0; i < t.getActualTypeArguments().length; i++) {

            Type t1 = t.getActualTypeArguments()[i];

            Type replace = visit(t1);
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
