package scraper.api.exceptions;

import scraper.annotations.NotNull;

/**
 * Exception thrown if bad IO happens at runtime
 */
public class NodeException extends RuntimeException {
    public NodeException(@NotNull Exception e, @NotNull String message) { super(message, e); }
    public NodeException(@NotNull String message) { super(message); }
}