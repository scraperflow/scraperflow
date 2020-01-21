package scraper.plugins.core.flowgraph.api;


import scraper.annotations.NotNull;
import scraper.api.node.Address;

/**
 * Describes a single edge form one node to another
 *
 * @since 1.0.0
 */
public interface ControlFlowEdge {
    @NotNull Address getFromAddress();
    @NotNull Address getToAddress();

    /** Middle display string */
    @NotNull String getDisplayLabel();

    /** Indicates if the edge is used multiple times from the origin node */
    boolean isMultiple();

    /** Indicates if the edge dispatches new Flow to the next nodes */
    boolean isDispatched();
}

