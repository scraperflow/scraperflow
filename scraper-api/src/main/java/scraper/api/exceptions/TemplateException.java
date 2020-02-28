package scraper.api.exceptions;

import scraper.annotations.NotNull;

/**
 * Exception thrown if bad template evaluation happens at runtime
 */
public class TemplateException extends RuntimeException {
    public TemplateException(@NotNull Exception e, @NotNull String message) { super(message, e); }
    public TemplateException(@NotNull String message) { super(message); }
}