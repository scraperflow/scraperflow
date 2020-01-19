package scraper.nodes.core.test.annotations;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Functional {
    Class<?> value();
    Class<?> expectException() default void.class;
}
