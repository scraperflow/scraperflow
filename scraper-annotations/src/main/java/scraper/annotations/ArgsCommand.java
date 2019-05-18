package scraper.annotations;

import java.lang.annotation.*;

/**
 * This annotation is used to document command line arguments which can be passed to Scraper.
 * It can be used multiple times if the class has multiple command line arguments to offer.
 *
 * @see ArgsCommands
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ArgsCommands.class)
public @interface ArgsCommand {
    /** The expected command-line format, i.e. a synopsis */
    String value();
    /** Documentation of the argument */
    String doc();
    /** Example usage of the argument */
    String example();
}
