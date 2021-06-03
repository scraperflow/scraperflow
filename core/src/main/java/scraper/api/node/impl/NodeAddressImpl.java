package scraper.api.node.impl;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.Address;
import scraper.api.NodeAddress;
import scraper.util.NodeUtil;

import java.util.Optional;

public class NodeAddressImpl implements NodeAddress {
    @NotNull private final String instance;
    @NotNull private final String graph;
    private final String label;
    /** Implementation detail of current workflows, used for debugging purposes and only available for NodeAddress of nodes, not for targeting */
    @NotNull private final Integer index;

    public NodeAddressImpl(@NotNull String instance, @NotNull String graph, @Nullable String label, @NotNull Integer stageIndex) {
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
        return new AddressImpl(instance+"."+graph+"."+(index+1));
    }

    @NotNull
    @Override
    public Address replace(@NotNull String representation) {
        return new AddressImpl(instance+"."+graph+"."+representation);
    }

    @NotNull
    @Override
    public String getNode() {
        return "" + (label == null ? index :
                label + (":" + index)
        );
    }

    @NotNull
    @Override
    public String getGraph() {
        return graph;
    }

    @NotNull
    @Override
    public String getInstance() {
        return instance;
    }

    @NotNull
    @Override
    public Integer getIndex() {
        return index;
    }

    @NotNull
    @Override
    public Optional<String> getLabel() {
        return Optional.ofNullable(label);
    }

    @NotNull
    @Override
    public Address getOnlyIndex() {
        return new AddressImpl(instance+"."+graph+"."+index);
    }

    @NotNull
    @Override
    public Optional<Address> getOnlyLabel() {
        if(label != null) {
            return Optional.of(new AddressImpl(instance + "." + graph + "." + label));
        }

        return Optional.empty();
    }
}
