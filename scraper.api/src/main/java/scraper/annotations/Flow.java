package scraper.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Compose nodes
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Flow {
    String label();
    boolean enumerate() default false;
}
