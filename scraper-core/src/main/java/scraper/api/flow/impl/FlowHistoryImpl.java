package scraper.api.flow.impl;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.flow.FlowHistory;
import scraper.api.flow.FlowState;
import scraper.api.node.NodeAddress;

import java.util.LinkedList;
import java.util.List;

public class FlowHistoryImpl implements FlowHistory {
    private @NotNull final List<FlowState> history = new LinkedList<>();
    private @Nullable NodeAddress firstAcceptingNode = null;
    private @NotNull String jobName = "Not active yet";

    @NotNull @Override public List<FlowState> getFlowHistory() { return history; }
    @NotNull @Override public String getJobName() { return jobName; }
    @Nullable @Override public NodeAddress getFirstAcceptingNode() { return firstAcceptingNode; }

    public void init(NodeAddress firstAcceptingNode, String jobName) {
        this.firstAcceptingNode = firstAcceptingNode;
        this.jobName = jobName;
    }
}
