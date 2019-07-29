package scraper.api.exceptions;

/**
 * Exception thrown if bad template evaluation happens
 *
 * @since 1.0.0
 */
public class TemplateException extends RuntimeException {
    public TemplateException(String message, Exception e) { super(message, e); }
    public TemplateException(String message) { super(message); }
}