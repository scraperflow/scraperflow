package scraper.api.node.impl;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.node.Address;
import scraper.api.node.NodeAddress;

import java.util.UUID;

public class NodeAddressImpl implements NodeAddress {
    @NotNull private final String instance;
    @NotNull private final String graph;
    @Nullable private final String label;
    /** Implementation detail of current workflows, used for debugging purposes and only available for NodeAddress of nodes, not for targeting */
    private final int index;

    private UUID id = UUID.randomUUID();

    public NodeAddressImpl(@NotNull String instance, @NotNull String graph, @Nullable String label, int stageIndex) {
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
                (label==null?index:label+":"+index)
                ;
    }

    @Override
    public Address resolve(Address toResolve) {
        throw new IllegalStateException();
    }
}
