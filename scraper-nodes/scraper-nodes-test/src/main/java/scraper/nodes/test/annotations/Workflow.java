package scraper.nodes.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Workflow {
    String value();
    String[] argumentFiles() default {};
    Class expectToFail() default void.class;
}
