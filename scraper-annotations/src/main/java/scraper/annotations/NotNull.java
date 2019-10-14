package scraper.annotations;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.CLASS;

/** Custom NotNull annotation to not depend on other packages */
@Retention(value=CLASS)
@Target(value={FIELD,METHOD,PARAMETER,LOCAL_VARIABLE})
public @interface NotNull {}

