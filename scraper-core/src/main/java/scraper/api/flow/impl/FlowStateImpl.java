package scraper.api.flow.impl;

import scraper.api.flow.FlowState;

import java.util.Objects;

public class FlowStateImpl implements FlowState {
    private final int stageIndex;
    private final String label;
    private final String jobName;

    public FlowStateImpl(int stageIndex, String label, String jobName) {
        this.stageIndex = stageIndex;
        this.label = label;
        this.jobName = jobName;
    }

    public static FlowState initial() {
        return new FlowStateImpl(-1, "unknown", "unknown");
    }

    @Override
    public int getStageIndex() {
        return stageIndex;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getJobName() {
        return jobName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowStateImpl flowState = (FlowStateImpl) o;
        return stageIndex == flowState.stageIndex &&
                label.equals(flowState.label) &&
                jobName.equals(flowState.jobName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stageIndex, label, jobName);
    }
}
