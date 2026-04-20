package scraper.api.node.impl;

import scraper.annotations.NotNull;
import scraper.api.Address;
import scraper.util.NodeUtil;

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

    @NotNull
    @Override
    public String getRepresentation() {
        return toString().substring(1,toString().length()-1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if(o instanceof Address) {
            Address that = (Address) o;
            return NodeUtil.representationEquals(getRepresentation(), that.getRepresentation());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return NodeUtil.representationHashCode(getRepresentation());
    }
}
