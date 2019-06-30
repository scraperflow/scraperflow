package scraper.api.node.impl;

import scraper.annotations.Nullable;
import scraper.api.node.NodeAddress;

import java.util.Objects;

public class NodeAddressImpl implements NodeAddress {
    // ============
    // Getter
    // ============
    /** Unique node label used for control flow */
    private String label;

    public NodeAddressImpl(@Nullable String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeAddressImpl that = (NodeAddressImpl) o;
        return Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
