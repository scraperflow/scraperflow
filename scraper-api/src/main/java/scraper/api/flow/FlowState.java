package scraper.api.flow;

import scraper.api.node.NodeAddress;

/**
 * Manages the current state of the flo
 *
 * @since 1.0.0
 */
public interface FlowState {
    /** Label of the last node that accepted this flow map */
    NodeAddress getAddress();

    /** Name of the job this flow state belongs to */
    String getJobName();
}
