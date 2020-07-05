package scraper.api.template;

import scraper.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Token class which saves the type with generics at runtime.
 * Used as a slim and minimal implementation to supply node implementors with a minimal set of Jars.
 */
public abstract class T<TYPE> implements Supplier<Type> {
	// type with generics
	protected final Type type;
	private final String typeSuffix;

	// parsed JSON object which is supplied at a later time
	protected Term<TYPE> term;

    public T() { this.type = resolveType(); typeSuffix = ""; }
	public T(Type t) { this.type = t; typeSuffix = ""; }
	public T(Type t, String typeSuffix) {
    	this.type = t;
    	this.typeSuffix = typeSuffix;
    }


    @Override @NotNull public Type get() { return type; }

	// this resolves and saves the type with generics at runtime
	// enables reasoning about generics even with Java type erasure, to an extent
	protected Type resolveType() {
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
			return Objects.equals(type, that.type) && Objects.equals(term, that.term);
		}
		return false;
	}

	public boolean equalsType(T<?> o) {
    	try {
			new TypeMatcher() {}.visitAndReturnCaptures(get(), o.get());
			return true;
		} catch (Exception e){
    		return false;
		}
	}

	@Override public int hashCode() { return Objects.hash(type, term); }

	@Override public String toString() {
		if(term == null) return get().toString();
		return term.toString();
	}

	public void setTerm(Term<TYPE> term) { this.term = term; }
	public Term<TYPE> getTerm(){ return term; }

	public String getTypeString() {
		if(!typeSuffix.isEmpty()) {
			return get().getTypeName() +"$"+typeSuffix;
		}
		if(getTerm() != null) return getTerm().getTypeString();
		return get().getTypeName();
	}


	public String getSuffix() {
		return typeSuffix;
	}


	public static Class<?> rawType(Type t) {
		if(t instanceof Class) {
			return (Class<?>) t;
		}

		if (t instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) t).getRawType();
		}

		return Object.class;
	}

	public Class<?> getRawType() {
    	return rawType(this.get());
	}
}