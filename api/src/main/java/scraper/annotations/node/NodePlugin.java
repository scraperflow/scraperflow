package scraper.annotations.node;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Nodes annotated as a NodePlugin will be collected and provided to the framework at runtime.
 * Exceptions are nodes which are marked as deprecated.
 * <p>
 * Nodes use semantic versioning.
 * Before increasing the major version of a node, archive the old version first to enable legacy node versioning.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface NodePlugin {
    /** Semantic node version of this node plugin */
    String value() default "0.0.0";

    /**
     * Nodes marked as deprecated will not be included
     * and are likely to be upgraded to a next major version or deleted completely.
     */
    boolean deprecated() default false;
}
