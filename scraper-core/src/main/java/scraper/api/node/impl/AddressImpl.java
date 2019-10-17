package scraper.api.node.impl;

import scraper.annotations.NotNull;
import scraper.api.node.Address;

import java.util.Objects;

public class AddressImpl implements Address {
    /** Unique label */
    private @NotNull String label;

    public AddressImpl(@NotNull String label) {
        this.label = label;
    }

    @NotNull public String getLabel() {
        return label;
    }
    public void setLabel(@NotNull String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "<"+label+">";
    }

    @Override
    public String getRepresentation() {
        return toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressImpl address = (AddressImpl) o;
        return label.equals(address.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }
}
