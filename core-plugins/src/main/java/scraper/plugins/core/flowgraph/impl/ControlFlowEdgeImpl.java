package scraper.plugins.core.flowgraph.impl;


import scraper.annotations.NotNull;
import scraper.api.node.NodeAddress;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;

/**
 * @since 1.0.0
 */
public class ControlFlowEdgeImpl implements ControlFlowEdge {
    public void setFromAddress(NodeAddress fromAddress) { this.fromAddress = fromAddress; }
    public void setToAddress(NodeAddress toAddress) { this.toAddress = toAddress; }

    @NotNull
    private NodeAddress fromAddress;
    @NotNull
    private NodeAddress toAddress;
    @NotNull
    private String displayLabel;

    private final boolean multiple;
    private final boolean dispatched;
    private final boolean propagate;

    public ControlFlowEdgeImpl(
            @NotNull final NodeAddress from,
            @NotNull final NodeAddress to,
            @NotNull String displayName,
            boolean multiple,
            boolean dispatched,
            boolean propagate
    ) {
        this.fromAddress = from;
        this.toAddress = to;
        this.displayLabel = displayName;
        this.multiple = multiple;
        this.dispatched = dispatched;
        this.propagate = propagate;
    }

    public ControlFlowEdgeImpl(
            @NotNull final NodeAddress from,
            @NotNull final NodeAddress to,
            @NotNull String displayName,
            boolean multiple,
            boolean dispatched
    ) {
        this(from, to, displayName, multiple, dispatched, false);
    }

    @NotNull
    @Override public String getDisplayLabel() { return displayLabel; }


    @NotNull
    @Override public NodeAddress getFromAddress() { return fromAddress; }
    @NotNull
    @Override public NodeAddress getToAddress() { return toAddress; }
    @Override public boolean isMultiple() { return multiple; }
    @Override public boolean isDispatched() { return dispatched; }

    public static ControlFlowEdge edge(@NotNull final NodeAddress from, @NotNull final NodeAddress to, @NotNull String displayLabel, boolean propagate) {
        return new ControlFlowEdgeImpl(from, to, displayLabel, false, false, propagate);
    }

    public static ControlFlowEdge edge(@NotNull final NodeAddress from, @NotNull final NodeAddress to, @NotNull String displayLabel) {
        return new ControlFlowEdgeImpl(from, to, displayLabel, false, false);
    }

    public static ControlFlowEdge edge(@NotNull final NodeAddress from, @NotNull final NodeAddress to, @NotNull String displayLabel, boolean multiple, boolean dispatched) {
        return new ControlFlowEdgeImpl(from, to, displayLabel, multiple, dispatched);
    }

    @Override
    public String toString() {
        return "<"+fromAddress +":" + displayLabel + ":" + toAddress+">";
    }


    @Override
    public boolean isPropagate() {
        return propagate;
    }
}

