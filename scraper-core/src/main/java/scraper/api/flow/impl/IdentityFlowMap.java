package scraper.api.flow.impl;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.flow.FlowHistory;
import scraper.api.flow.FlowMap;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class IdentityFlowMap implements FlowMap {

    @Override public @Nullable Object put(@NotNull String location, @NotNull Object value) { throw new IllegalStateException(); }
    @Override public void putAll(@NotNull Map<String, Object> m) { throw new IllegalStateException(); }
    @Override public @Nullable Object remove(@NotNull String location) { throw new IllegalStateException(); }
    @Override public int size() { throw new IllegalStateException(); }
    @Override public void clear() { throw new IllegalStateException(); }
    @Override public @NotNull Set<String> keySet() { throw new IllegalStateException(); }
    @NotNull @Override public FlowHistory getFlowHistory() { throw new IllegalStateException(); }
    @Override public UUID getId() { throw new IllegalStateException(); }
    public @NotNull ConcurrentMap<String, Object> getMap() { throw new IllegalStateException(); }
    public boolean containsElements(@NotNull final FlowMap expectedOutput) { throw new IllegalStateException(); }

    // return identity
    @Override public @Nullable Object get(@NotNull String expected) { return expected; }
    @Override public @NotNull Object getOrDefault(@NotNull Object key , @NotNull Object defaultObjectalue) { return key; }
}
