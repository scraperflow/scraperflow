package scraper.annotations.di;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface DITarget {
    Class<?> value();
}
