package scraper.api.exceptions;

import scraper.annotations.NotNull;

/**
 * Exception which can only be thrown during initialization of a scrape specification.
 *
 * @since 1.0.0
 */
public class ValidationException extends Exception {
    public ValidationException(@NotNull String message) { super(message); }
    public ValidationException(@NotNull String message, @NotNull Exception reason) { super(message, reason); }
}