package scraper.api.exceptions;

import scraper.annotations.NotNull;

/**
 * Exception thrown if bad template evaluation happens
 *
 * @since 1.0.0
 */
public class TemplateException extends RuntimeException {
    public TemplateException(@NotNull String message, @NotNull Exception e) { super(message, e); }
    public TemplateException(@NotNull String message) { super(message); }
}