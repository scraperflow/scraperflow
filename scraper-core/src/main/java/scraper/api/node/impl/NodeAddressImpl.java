package scraper.api.node.impl;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.node.NodeAddress;

import java.util.Objects;

public class NodeAddressImpl implements NodeAddress {
    @NotNull private final String instance;
    @NotNull private final String graph;
    @Nullable private final String label;
    /** Implementation detail of current workflows, used for debugging purposes and only available for NodeAddress of nodes, not for targeting */
    @Nullable private final Integer index;

    public NodeAddressImpl(@NotNull String instance, @NotNull String graph, @Nullable String label, @Nullable Integer stageIndex) {
        if(label == null && stageIndex == null) throw new IllegalStateException("Either label or index can be null, not both");

        this.instance = instance;
        this.graph = graph;
        this.label = label;
        index = stageIndex;
    }

    @Override
    public String toString() {
        return "<"+getRepresentation()+">";
    }

    @Override
    public String getRepresentation() {
        return instance +"." + graph+"."+
                (label==null?index:
                        label+(index != null ? ":"+index : "")
                )
                ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeAddressImpl that = (NodeAddressImpl) o;
        return instance.equals(that.instance) &&
                graph.equals(that.graph) &&
                Objects.equals(label, that.label) &&
                Objects.equals(index, that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instance, graph, label, index);
    }
}
