package scraper.api.flow;


import scraper.api.node.NodeAddress;

/**
 * Describes a single edge form one node to another
 *
 * @since 1.0.0
 */
public interface ControlFlowEdge {
    /** From address */
    NodeAddress getFromAddress();

    /** To address */
    NodeAddress getToAddress();

    /** Middle display string */
    String getDisplayLabel();

    /** Indicates if the edge is used multiple times from the origin node */
    boolean isMultiple();

    /** Indicates if the edge dispatches new Flow to the next nodes */
    boolean isDispatched();
}

