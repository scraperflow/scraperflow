package scraper.api.flow;

import scraper.annotations.NotNull;

import java.util.Map;

/**
 * Manages the current state of the flow
 *
 * @since 1.0.0
 */
public interface FlowState {

    /**
     * The higher the log level of a node is, the more information will be put into the flow state
     *
     * For TRACE, the whole flow map content is copied into the history
     * For DEBUG, only top-level values are inserted and trimmed if necessary
     * For INFO, only keys without values are tracked
     * For WARN, only history of node accesses is tracked
     * For ERROR, nothing is tracked
     */
    @NotNull Map<String, Object> getState();

    /** Which phase of the accepting node this state was captured */
    @NotNull String getPhase();

    void log(@NotNull String key, @NotNull Object log);
}
