package scraper.api.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class T<TYPE> implements Supplier<Type> {

	private final Type type;

	@Override
	public String toString() {
		return parsedJson.toString();
	}

	// parsed JSON object
	private Object parsedJson;

	public void setParsedJson(Object convertedTemplateObject) {
		this.parsedJson = convertedTemplateObject;
	}
	public Object getParsedJson(){
		return parsedJson;
	}

	public T() {
		this.type = resolveType();
	}

	@Override
	public final Type get() {
		return type;
	}

//	@Override
//	public final String toString() {
//		return type.toString();
//	}

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

	@Override
	public int hashCode() {
		return Objects.hash(type, parsedJson);
	}

	public String getRawJson() {
		return parsedJson.toString();
	}
}