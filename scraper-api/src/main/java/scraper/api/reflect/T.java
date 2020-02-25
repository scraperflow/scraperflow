package scraper.api.reflect;

import scraper.annotations.NotNull;
import scraper.api.flow.FlowMap;

import java.lang.reflect.*;
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
	private Term<TYPE> term;

	public T() { this.type = resolveType(); }
	public T(Type t) { this.type = t; }
	// can be used for location T
	public T(Term<TYPE> location) { this.type = resolveType(); this.term = location; }
	public T(String constant) { this.type = resolveType();
		T<TYPE> ref = this;
		this.term = new Primitive<>() {
		@Override public void accept(TVisitor visitor) { visitor.visitPrimitive(this); }
		@SuppressWarnings("unchecked") // TODO think about how to describe locations and expected types without abusing T
		@Override public TYPE eval(FlowMap o) { return (TYPE) constant; }
		@Override public Object getRaw() { return constant; }
			@Override public T<TYPE> getToken() { throw new IllegalStateException(); }
		}; }

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

	public static <A> Class<A> getRawType(Type type) {
		if (type instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) type).getGenericComponentType();
			Class<?> componentClass = getRawType(componentType);
			if (componentClass != null) {
				@SuppressWarnings("unchecked")
				Class<A> claz = (Class<A>) Array.newInstance(componentClass, 0).getClass();
				return claz;
			} else throw new UnsupportedOperationException("Unknown class: " + type.getClass());
		} else if (type instanceof Class) {
			@SuppressWarnings("unchecked") // raw type is |A|
			Class<A> claz = (Class<A>) type;
			return claz;
		} else if (type instanceof ParameterizedType) {
			return getRawType(((ParameterizedType) type).getRawType());
		} else if (type instanceof TypeVariable) {
			throw new RuntimeException("The type signature is erased. The type class cant be known by using reflection");
		} else throw new UnsupportedOperationException("Unknown class: " + type.getClass());
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof T) {
			T<?> that = (T<?>) o;
			return Objects.equals(type, that.type) && Objects.equals(term, that.term);
		}
		return false;
	}

	@Override public int hashCode() { return Objects.hash(type, term); }

	@Override public String toString() {
		if(term == null) return get().toString();
		return term.toString();
	}

	// Getter, setter
	public void setTerm(Term<TYPE> term) { this.term = term; }
	public Term<TYPE> getTerm(){ return term; }

}