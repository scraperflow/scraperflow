package scraper.annotations.node;

import scraper.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Indicates that this field will be parsed from a scrape specification.
 * If mandatory is enabled, it enforces the key to exist in the specification.
 *
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface FlowKey {
    /** If enabled, forces the key to exist in the scrape specification */
    boolean mandatory() default false;

    /** The default value of a JSON flow key if no value is provided */
    @NotNull String defaultValue() default "null";

    /**
     * If enabled, the generic type of the flow key specifies the class to be put into a flow map
     * instead of the class the template is going to evaluate to
     */
    boolean output() default false;
}
