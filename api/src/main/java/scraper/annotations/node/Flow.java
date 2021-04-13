package scraper.annotations.node;

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
