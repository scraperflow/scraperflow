package scraper.api.flow;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.node.NodeAddress;

import java.util.List;

/**
 * Manages the current history of the flow
 *
 * @since 1.0.0
 */
public interface FlowHistory {
    /**
     * Ordered history of the flow since the flow was created
     *
     * The higher the log level of a node is, the more information will be put into the flow history
     *
     * For TRACE, the whole flow map content is copied into the history
     * For DEBUG, only top-level values are inserted and trimmed if necessary
     * For INFO, only keys without values are tracked
     * For WARN, only history of node accesses is tracked
     * For ERROR, nothing is tracked
     */
    @NotNull List<FlowState> getFlowHistory();

    /** Origin of the flow. Is set once the flow is dispatched into action */
    @Nullable NodeAddress getFirstAcceptingNode();

    /** Name of the job this flow state belongs to */
    @NotNull String getJobName();
}
