package scraper.api.node.impl;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.node.Address;
import scraper.api.node.NodeAddress;
import scraper.util.NodeUtil;

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

    @NotNull
    @Override
    public String getRepresentation() {
        return instance +"." + graph+"."+ getNode();
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

    @NotNull
    @Override
    public Address nextIndex() {
        assert index != null;
        return new AddressImpl(instance+"."+graph+"."+(index+1));
    }

    @NotNull
    @Override
    public Address replace(@NotNull String representation) {
        return new AddressImpl(instance+"."+graph+"."+representation);
    }

    @Override
    public String getNode() {
        return ""+(label==null?index:
                label+(index != null ? ":"+index : "")
        );
    }

    @Override
    public String getGraph() {
        return graph;
    }

    @Override
    public String getInstance() {
        return instance;
    }
}
