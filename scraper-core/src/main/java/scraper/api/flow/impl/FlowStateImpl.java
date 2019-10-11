package scraper.api.flow.impl;

import scraper.annotations.NotNull;
import scraper.api.flow.FlowState;

import java.util.HashMap;
import java.util.Map;

public class FlowStateImpl implements FlowState {
    private final Map<String, Object> state = new HashMap<>();
    private final String phase;

    public FlowStateImpl(@NotNull String phase) {
        this.phase = phase;
    }

    @NotNull
    @Override
    public Map<String, Object> getState() {
        return state;
    }

    @NotNull
    @Override
    public String getPhase() {
        return phase;
    }

    @Override
    public void log(@NotNull String key, @NotNull Object log) {
        state.put(key, log);
    }

}
