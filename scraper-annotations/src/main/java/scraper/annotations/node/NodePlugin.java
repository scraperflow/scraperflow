package scraper.annotations.node;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Nodes annotated as a NodePlugin will be included in the generated editor.
 * Exceptions are nodes which are marked as deprecated.
 * <p>
 * Nodes use semantic versioning.
 * Before increasing the major version of a node, archive the version first to enable legacy node versioning.
 *
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface NodePlugin {
    /** Semantic node version of this node plugin */
    String value() default "0.0.0";

    /** Nodes marked as deprecated will not be included in the editor and are likely to be upgraded to a next major version or deleted completely */
    boolean deprecated() default false;

    /** Indicates if a node keeps a hidden state which can be manipulated by accepting flows */
    boolean stateful() default false;
}
