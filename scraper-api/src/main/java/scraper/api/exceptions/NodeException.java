package scraper.api.exceptions;

/**
 * A general exception if something goes wrong during a flow.
 *
 * @since 1.0.0
 */
public class NodeException extends Exception {
    public NodeException(String message) { this(null, message); }
    public NodeException(Exception cause, String message) { super(message, cause); }
}