package scraper.api.exceptions;

import scraper.annotations.NotNull;

/**
 * A general exception if something goes wrong during a flow.
 */
public class NodeException extends Exception {
    public NodeException(@NotNull String message) { super(message); }
    public NodeException(@NotNull Exception cause, @NotNull String message) { super(message, cause); }
}