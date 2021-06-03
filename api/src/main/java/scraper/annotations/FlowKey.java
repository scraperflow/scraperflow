package scraper.annotations;

import scraper.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Indicates that this field will be parsed from a scrape specification.
 * If mandatory is enabled, it enforces the key to exist in the specification.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface FlowKey {
    /** If enabled, forces the key to exist in the scrape specification */
    boolean mandatory() default false;

    /** The default value of a JSON flow key if no value is provided */
    @NotNull String defaultValue() default "null";
}
