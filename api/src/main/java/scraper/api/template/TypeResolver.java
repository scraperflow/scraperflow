package scraper.api.template;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class TypeResolver {

    public static Type listType(Type elementType) {
        return new ParameterizedType() {
            @Override public Type[] getActualTypeArguments() { return new Type[]{elementType}; }
            @Override public Type getRawType() { return List.class; }
            @Override public Type getOwnerType() { return null; }
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

    public static Type mapType(Type elementType, Type valueType) {
        return new ParameterizedType() {
            @Override public Type[] getActualTypeArguments() { return new Type[]{elementType, valueType}; }
            @Override public Type getRawType() { return Map.class; }
            @Override public Type getOwnerType() { return null; }
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
                        .map(t -> t.getTypeName())
                        .collect(Collectors.joining(", "));
                return getRawType().getTypeName()+"<"+arr+">";
            }
        };
    }
}
