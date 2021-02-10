package scraper.api.exceptions;

import scraper.annotations.NotNull;

/**
 * A general exception if the node should stop execution and forwarding in the accept phase immediately and return.
 * Hooks that were not executed for that node are be skipped.
 */
public class BreakException extends NodeException {
    public BreakException(@NotNull String message) { super(message); }
    public BreakException(@NotNull Exception cause, @NotNull String message) { super(cause, message); }
}