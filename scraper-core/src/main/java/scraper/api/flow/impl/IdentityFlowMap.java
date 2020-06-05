package scraper.api.flow.impl;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.flow.FlowMap;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class IdentityFlowMap implements FlowMap {

    @NotNull @Override public Optional<Object> get(@NotNull String key) {
        return Optional.empty();
    }

    @NotNull
    @Override
    public <K> Optional<K> getWithType(@NotNull String targetKey, @NotNull T<K> targetType) {
        return Optional.empty();
    }

    @Override public int size() { throw new IllegalStateException(); }
    @Override public void clear() { throw new IllegalStateException(); }

    @Override
    public <A> void output(@NotNull L<A> locationAndType, @Nullable A outputObject) {

    }

    @Override public @NotNull Set<String> keySet() { throw new IllegalStateException(); }

    @Override
    public void remove(@NotNull String key) {

    }

    @NotNull @Override public UUID getId() { throw new IllegalStateException(); }
    @NotNull
    @Override public <A> A eval(@NotNull T<A> template) { throw new IllegalStateException(); }

    @NotNull
    @Override
    public <A> String eval(@NotNull L<A> template) {
        return null;
    }

    @NotNull
    @Override
    public <A> Optional<String> evalMaybe(@NotNull L<A> template) {
        return Optional.empty();
    }

    @NotNull
    @Override
    public <A> Optional<A> evalMaybe(@NotNull T<A> template) {
        return Optional.empty();
    }

    @NotNull
    @Override public <A> A evalIdentity(@NotNull T<A> t) { throw new IllegalStateException(); }

    @NotNull
    @Override
    public <A> Optional<A> evalIdentityMaybe(@NotNull T<A> template) {
        throw new IllegalStateException();
    }

    @NotNull
    @Override
    public <A> A evalOrDefault(@NotNull T<A> template, @NotNull A defaultEval) {
        return null;
    }

    @Override
    public void output(@NotNull String location, Object outputObject) {

    }

    @NotNull
    @Override
    public FlowMap copy() {
        return null;
    }

    @NotNull
    @Override
    public FlowMap newFlow() {
        return null;
    }

    public @NotNull ConcurrentMap<String, Object> getMap() { throw new IllegalStateException(); }
    public boolean containsElements(@NotNull final FlowMap expectedOutput) { throw new IllegalStateException(); }

    @NotNull
    @Override
    public Optional<UUID> getParentId() {
        return Optional.empty();
    }

    @NotNull
    @Override
    public Optional<Integer> getParentSequence() {
        return Optional.empty();
    }

    @Override
    public int getSequence() {
        return 0;
    }

    @Override
    public void nextSequence() {

    }
}
