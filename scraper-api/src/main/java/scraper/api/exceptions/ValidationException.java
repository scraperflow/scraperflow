package scraper.api.exceptions;

/**
 * Exception which can only be thrown during initialization of a scrape specification.
 *
 * @since 1.0.0
 */
public class ValidationException extends Exception {
    public ValidationException(String message) { super(message); }
    public ValidationException(String message, Exception reason) { super(message, reason); }
}