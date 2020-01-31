package scraper.api.node.impl;

import scraper.annotations.NotNull;
import scraper.api.node.GraphAddress;

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
        if (o == null || getClass() != o.getClass()) return false;
        GraphAddressImpl that = (GraphAddressImpl) o;
        return instanceLabel.equals(that.instanceLabel) &&
                graphLabel.equals(that.graphLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceLabel, graphLabel);
    }
}
