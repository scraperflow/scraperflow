package scraper.api.flow.impl;

import scraper.api.flow.FlowState;
import scraper.api.node.NodeAddress;
import scraper.util.NodeUtil;

import java.util.Objects;

public class FlowStateImpl implements FlowState {
    private final NodeAddress address;
    private final String jobName;

    public FlowStateImpl(NodeAddress address, String jobName) {
        this.address = address;
        this.jobName = jobName;
    }

    public static FlowState initial() {
        return new FlowStateImpl(NodeUtil.addressOf("unknown"), "unknown");
    }

    @Override
    public NodeAddress getAddress() {
        return address;
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
        return address.equals(flowState.address) &&
                jobName.equals(flowState.jobName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address.getLabel(), jobName);
    }
}
