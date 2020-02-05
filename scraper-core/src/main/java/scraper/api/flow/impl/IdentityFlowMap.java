package scraper.api.flow.impl;

import scraper.annotations.NotNull;
import scraper.api.flow.FlowHistory;
import scraper.api.flow.FlowMap;
import scraper.api.reflect.T;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class IdentityFlowMap implements FlowMap {

    @NotNull
    @Override
    public Optional<Object> get(@NotNull String key) {
        return Optional.empty();
    }

    @NotNull
    @Override
    public Object getOrDefault(@NotNull Object key, @NotNull Object defaultObjectValue) {
        return defaultObjectValue;
    }

    //    @Override public @Nullable Object put(@NotNull String location, @NotNull Object value) { throw new IllegalStateException(); }
//    @Override public void putAll(@NotNull Map<String, Object> m) { throw new IllegalStateException(); }
//    @Override public @Nullable Object remove(@NotNull String location) { throw new IllegalStateException(); }
    @Override public int size() { throw new IllegalStateException(); }
    @Override public void clear() { throw new IllegalStateException(); }
    @Override public @NotNull Set<String> keySet() { throw new IllegalStateException(); }

    @NotNull
    @Override
    public Optional<?> put(@NotNull String key, @NotNull Object value) {
        return Optional.empty();
    }

    @Override
    public void putAll(@NotNull Map<String, Object> m) {

    }

    @NotNull
    @Override
    public Optional<Object> remove(@NotNull String key) {
        return Optional.empty();
    }

    @NotNull @Override public FlowHistory getFlowHistory() { throw new IllegalStateException(); }
    @NotNull @Override public UUID getId() { throw new IllegalStateException(); }
    @NotNull
    @Override public <A> A eval(@NotNull T<A> template) { throw new IllegalStateException(); }

    @NotNull
    @Override
    public <A> Optional<A> evalMaybe(@NotNull T<A> template) {
        return Optional.empty();
    }

//    @Override public <A> A evalOrDefault(@NotNull T<A> template, A object) { throw new IllegalStateException(); }

    @NotNull
    @Override public <A> A evalIdentity(@NotNull T<A> t) { throw new IllegalStateException(); }

    @NotNull
    @Override
    public <A> A evalOrDefault(@NotNull T<A> template, @NotNull A defaultEval) {
        return null;
    }

    @Override public <A> void output(@NotNull T<A> locationAndType, A object) { throw new IllegalStateException(); }
    public @NotNull ConcurrentMap<String, Object> getMap() { throw new IllegalStateException(); }
    public boolean containsElements(@NotNull final FlowMap expectedOutput) { throw new IllegalStateException(); }

    // return identity
//    @Override public @Nullable Object get(@NotNull String expected) { return expected; }
//    @Override public @NotNull Object getOrDefault(@NotNull Object key , @NotNull Object defaultObjectalue) { return key; }
}
