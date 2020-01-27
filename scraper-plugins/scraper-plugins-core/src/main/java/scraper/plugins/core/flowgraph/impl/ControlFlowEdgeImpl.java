package scraper.plugins.core.flowgraph.impl;


import scraper.annotations.NotNull;
import scraper.api.node.Address;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;

/**
 * @since 1.0.0
 */
public class ControlFlowEdgeImpl implements ControlFlowEdge {
    @NotNull private final Address fromAddress;
    @NotNull private final Address toAddress;
    @NotNull private final String displayLabel;

    private final boolean multiple;
    private final boolean dispatched;

    public ControlFlowEdgeImpl(
            @NotNull final Address from,
            @NotNull final Address to,
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


    @NotNull @Override public Address getFromAddress() { return fromAddress; }
    @NotNull @Override public Address getToAddress() { return toAddress; }
    @Override public boolean isMultiple() { return multiple; }
    @Override public boolean isDispatched() { return dispatched; }

    public static ControlFlowEdge edge(@NotNull final Address from, @NotNull final Address to, @NotNull String displayLabel) {
        return new ControlFlowEdgeImpl(from, to, displayLabel, false, false);
    }

    public static ControlFlowEdge edge(@NotNull final Address from, @NotNull final Address to, @NotNull String displayLabel, boolean multiple, boolean dispatched) {
        return new ControlFlowEdgeImpl(from, to, displayLabel, multiple, dispatched);
    }

    @Override
    public String toString() {
        return "<"+fromAddress +":" + displayLabel + ":" + toAddress+">";
    }
}

