package scraper.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface TranslationInput {
    boolean fail() default false;
    String jsonValue() default "";
    String globalValue() default "";
}
