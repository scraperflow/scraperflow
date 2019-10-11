package scraper.api.flow.impl;


import scraper.annotations.NotNull;
import scraper.api.flow.ControlFlowEdge;
import scraper.api.node.NodeAddress;

/**
 * @since 1.0.0
 */
public class ControlFlowEdgeImpl implements ControlFlowEdge {
    @NotNull private final NodeAddress fromAddress;
    @NotNull private final NodeAddress toAddress;
    @NotNull private final String displayLabel;

    private final boolean multiple;
    private final boolean dispatched;

    public ControlFlowEdgeImpl(
            @NotNull final NodeAddress from,
            @NotNull final NodeAddress to,
            @NotNull String displayName,
            boolean multiple,
            boolean dispatched

    ) {
        this.fromAddress = from;
        this.toAddress = to;
        this.displayLabel = displayName;
        this.multiple = multiple;
        this.dispatched = dispatched;
    }

    @NotNull @Override public String getDisplayLabel() { return displayLabel; }
    @NotNull @Override public NodeAddress getFromAddress() { return fromAddress; }
    @NotNull @Override public NodeAddress getToAddress() { return toAddress; }
    @Override public boolean isMultiple() { return multiple; }
    @Override public boolean isDispatched() { return dispatched; }

    public static ControlFlowEdge edge(@NotNull final NodeAddress from, @NotNull final NodeAddress to, @NotNull String displayLabel) {
        return new ControlFlowEdgeImpl(from, to, displayLabel, false, false);
    }

}

