package scraper.api.flow.impl;


import scraper.api.flow.ControlFlowEdge;

/**
 * @since 1.0.0
 */
public class ControlFlowEdgeImpl implements ControlFlowEdge {
    final String target;
    final String label;
    final String targetLabel;
    boolean multiple;
    boolean dispatched;

    public ControlFlowEdgeImpl(String target, String label, String targetLabel) {
        this.target = target;
        this.label = label;
        this.targetLabel = targetLabel;
    }

    public ControlFlowEdgeImpl(String target, String label, String targetLabel, Boolean dispatched) {
        this(target, label, targetLabel);
        this.dispatched = dispatched;
    }

    public String getTarget() {
        return this.target;
    }

    public String getLabel() {
        return this.label;
    }

    public String getTargetLabel() {
        return this.targetLabel;
    }

    public boolean isMultiple() {
        return this.multiple;
    }

    public boolean isDispatched() {
        return this.dispatched;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public void setDispatched(boolean dispatched) {
        this.dispatched = dispatched;
    }
}

