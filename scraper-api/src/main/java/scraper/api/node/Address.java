package scraper.api.node;

import scraper.annotations.NotNull;

/**
 * Address which can be either an instance, graph, or node address
 */
public interface Address {
    /** Unique string representation */
    @NotNull String getRepresentation();

}
