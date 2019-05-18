package scraper.annotations.node;

import java.lang.annotation.*;

/**
 * Indicates that this field will be parsed from a JSON specification.
 * If mandatory is enabled, it enforces the key to exist in the specification.
 *
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface FlowKey {
    /** If enabled, forces the key to exist in the JSON specification */
    boolean mandatory() default false;

    /** The default value of a JSON flow key if no value is provided */
    String defaultValue() default "null";

    /** The default value of a JSON flow value for a MapKey location, if that location is null */
    String mapkeyValue() default "null";
}
