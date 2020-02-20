package scraper.api.reflect;

import scraper.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Token class which saves the type with generics at runtime.
 * Used as a slim and minimal implementation to supply node implementors with a minimal set of Jars.
 * Actual resolving of types is done via Guava's TypeTokens, which is a bigger library,
 * but not needed to implement nodes.
 */
public abstract class T<TYPE> implements Supplier<Type> {
	// type with generics
	private final Type type;

	// parsed JSON object which is supplied at a later time
	private Object parsedJson;

	public T() { this.type = resolveType(); }
	// can be used for location T
	public T(String location) { this.type = resolveType(); this.parsedJson = location; }

	@Override @NotNull public Type get() { return type; }

	// this resolves and saves the type with generics at runtime
	// enables reasoning about generics even with Java type erasure, to an extent
	// you still can't generate types at runtime,
	// but nodes can at least specify their template generic types at compile time to be used at runtime
	private Type resolveType() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final Class<T<TYPE>> superclass = (Class) T.class;
		@SuppressWarnings("unchecked")
		final Class<? extends T<TYPE>> thisClass = (Class<T<TYPE>>) getClass();
		final Class<?> actualSuperclass = thisClass.getSuperclass();
		if ( actualSuperclass != superclass ) {
			throw new IllegalArgumentException(thisClass + " must extend " + superclass + " directly but it extends " + actualSuperclass);
		}
		final Type genericSuperclass = thisClass.getGenericSuperclass();
		if ( !(genericSuperclass instanceof ParameterizedType) ) {
			throw new IllegalArgumentException(thisClass + " must parameterize its superclass " + genericSuperclass);
		}
		final ParameterizedType parameterizedGenericSuperclass = (ParameterizedType) genericSuperclass;
		final Type[] actualTypeArguments = parameterizedGenericSuperclass.getActualTypeArguments();
		return actualTypeArguments[0];
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof T) {
			T<?> that = (T<?>) o;
			return Objects.equals(type, that.type) && Objects.equals(parsedJson, that.parsedJson);
		}
		return false;
	}

	@Override public int hashCode() { return Objects.hash(type, parsedJson); }

	@Override public String toString() {
		if(parsedJson == null) return get().toString();
		return parsedJson.toString();
	}

	// Getter, setter
	public void setParsedJson(Object convertedTemplateObject) { this.parsedJson = convertedTemplateObject; }
	public Object getParsedJson(){ return parsedJson; }
	public String getRawJson() { return parsedJson.toString(); }
}