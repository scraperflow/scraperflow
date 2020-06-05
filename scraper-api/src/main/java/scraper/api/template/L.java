package scraper.api.template;

import scraper.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Token class which saves the type with generics at runtime.
 * The type specifies the output type.
 */
public abstract class L<TYPE> extends T<TYPE> {
	private Term<String> termLocation;
	private T<?> ref;

	public L() { super(); }
	public L(Type t) { super(t); }
	public L(T<?> ref) {
		super();
		this.ref = ref;
	}

	@Override
	protected Type resolveType() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final Class<L<TYPE>> superclass = (Class) L.class;
		@SuppressWarnings("unchecked")
		final Class<? extends L<TYPE>> thisClass = (Class<L<TYPE>>) getClass();
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

	public void setLocation(Term<String> location) { termLocation = location; }
	public @NotNull Term<String> getLocation(){ return termLocation; }
}