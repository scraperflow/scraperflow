package scraper.api.node.impl;

import scraper.annotations.NotNull;
import scraper.api.node.Address;

public class GraphKeyAddress implements Address {
    /** Unique node label */
    private @NotNull String label;

    public GraphKeyAddress(@NotNull String label) {
        this.label = label;
    }

    @Override
    public boolean equalsTo(Address o) {
        return false;
    }
}
