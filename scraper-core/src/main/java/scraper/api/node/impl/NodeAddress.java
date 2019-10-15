package scraper.api.node.impl;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.node.Address;

import java.util.UUID;

public class NodeAddress implements Address {
    /** Unique node label */
    private @NotNull String label;

    /** Implementation detail of current workflows, used for debugging purposes and only available for NodeAddress of nodes, not for targeting */
    private @Nullable Integer index;

    public NodeAddress() {
        this(null, null);
    }

    public NodeAddress(@Nullable String label) {
        this(label, null);
    }

    public NodeAddress(@Nullable String label, @Nullable Integer stageIndex) {
        if(label == null) this.label = UUID.randomUUID().toString();
        else this.label = label;

        index = stageIndex;
    }

    @NotNull public String getLabel() {
        return label;
    }

    public void setLabel(@NotNull String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        if(index == null) {
            return "<"+label+">";
        } else {
            return "<"+label+"@"+index+">";
        }
    }
}
