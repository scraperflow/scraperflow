package scraper.annotations.di;

import scraper.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Searches for classes with target value at runtime and injects it into the annotated value
 *
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface DITarget {
    @NotNull Class<?> value();
}
