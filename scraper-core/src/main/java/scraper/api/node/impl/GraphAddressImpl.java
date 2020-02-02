package scraper.api.node.impl;

import scraper.annotations.NotNull;
import scraper.api.node.Address;
import scraper.api.node.GraphAddress;
import scraper.util.NodeUtil;

import java.util.Objects;

public class GraphAddressImpl implements GraphAddress {

    private final String instanceLabel;
    private final String graphLabel;

    public GraphAddressImpl(@NotNull String instance, @NotNull String graphLabel) {
        this.graphLabel = graphLabel;
        this.instanceLabel = instance;
    }

    @Override
    public String toString() {
        return "<"+getRepresentation()+">";
    }

    @NotNull
    @Override
    public String getRepresentation() {
        return instanceLabel+"."+graphLabel;
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
