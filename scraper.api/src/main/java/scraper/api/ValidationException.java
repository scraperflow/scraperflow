package scraper.api;

import scraper.annotations.NotNull;

/**
 * Exception which can only be thrown during initialization of a scrape specification.
 */
public class ValidationException extends Exception {
    public ValidationException(@NotNull String message) { super(message); }
    public ValidationException(@NotNull Exception reason, @NotNull String message) { super(message, reason); }
}