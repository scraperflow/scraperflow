package scraper.annotations.node;

import scraper.api.converter.StringToClassConverter;

import java.lang.annotation.*;

/**
 * Fields annotated with this annotation are evaluated as templates once during initialization.
 * A custom JSON converter can be provided,
 * which should provide a static convert method like in the {@link StringToClassConverter} class.
 *
 * @see StringToClassConverter
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Argument {
    /**
     * Target class which provides a static Object convert(String, Class<?>) method. Default implementation can handle
     * primitive types only.
     *
     * @see StringToClassConverter
     */
    Class<?> converter() default StringToClassConverter.class;
}

