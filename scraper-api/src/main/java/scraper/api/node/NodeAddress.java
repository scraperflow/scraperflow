package scraper.api.node;

import scraper.annotations.NotNull;

public interface NodeAddress {
    // ============
    // Getter
    // ============
    /** Unique node label used for control flow */
    @NotNull String getLabel();
}
