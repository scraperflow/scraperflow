package scraper.api;

import scraper.annotations.NotNull;

/**
 * Exception thrown if bad IO happens at runtime
 */
public class NodeIOException extends RuntimeException {
    public NodeIOException(@NotNull Exception e, @NotNull String message) { super(message, e); }
    public NodeIOException(@NotNull String message) { super(message); }
}