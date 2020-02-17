package scraper.annotations.node;

import java.lang.annotation.*;

/**
 * Annotated (String) fields are treated as file paths.
 * On Initialization and during runtime file existence (optionally with directory structure) is ensured.
 * If ensure fails, a NodeException is thrown.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnsureFile {
    /** If enabled ensures directory structure by creating parent directories first */
    boolean ensureDir() default true;
}
