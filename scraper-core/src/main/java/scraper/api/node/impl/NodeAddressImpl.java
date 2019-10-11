package scraper.api.node.impl;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.node.NodeAddress;

import java.util.Objects;
import java.util.UUID;

public class NodeAddressImpl implements NodeAddress {
    // ============
    // Getter
    // ============

    /** Unique node label used for control flow */
    private @NotNull String label;

    public NodeAddressImpl() {
        this(null);
    }

    public NodeAddressImpl(@Nullable String label) {
        if(label == null) this.label = UUID.randomUUID().toString();
        else this.label = label;
    }

    @NotNull @Override public String getLabel() {
        return label;
    }

    public void setLabel(@NotNull String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeAddressImpl that = (NodeAddressImpl) o;
        return label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }

    @Override
    public String toString() {
        return "<"+label+">";
    }
}
