package scraper.api.node.impl;

import scraper.annotations.Nullable;
import scraper.api.node.NodeAddress;

import java.util.UUID;

public class NodeAddressImpl extends AddressImpl implements NodeAddress {
    /** Implementation detail of current workflows, used for debugging purposes and only available for NodeAddress of nodes, not for targeting */
    private @Nullable Integer index;

    public NodeAddressImpl() { this(null, null); }
    public NodeAddressImpl(@Nullable String label) {
        this(label, null);
    }
    public NodeAddressImpl(@Nullable String label, @Nullable Integer stageIndex) {
        super(label == null ? UUID.randomUUID().toString() : label);
        index = stageIndex;
    }

    @Override
    public String toString() {
        if(index == null) {
            return "<"+getLabel()+">";
        } else {
            return "<"+getLabel()+"@"+index+">";
        }
    }

    @Override
    public int getIndex() {
        return index;
    }
}
