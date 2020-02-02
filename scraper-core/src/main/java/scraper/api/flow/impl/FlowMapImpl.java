package scraper.api.flow.impl;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.flow.FlowHistory;
import scraper.api.flow.FlowMap;
import scraper.api.reflect.T;
import scraper.core.Template;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FlowMapImpl implements FlowMap {

    private @NotNull final ConcurrentMap<String, Object> privateMap;
    private @NotNull final FlowHistory flowHistory = new FlowHistoryImpl();
    private @NotNull final UUID uuid = UUID.randomUUID();

    public FlowMapImpl(@NotNull ConcurrentMap<String, Object> privateMap) { this.privateMap = privateMap; }

    public FlowMapImpl() { privateMap = new ConcurrentHashMap<>(); }

    @Override
    public @Nullable Object put(@NotNull String location, @NotNull Object value) { return privateMap.put(location, value); }

    @Override
    public void putAll(@NotNull Map<String, Object> m) {
        privateMap.putAll(m);
    }

    @Override
    public @Nullable Object remove(@NotNull String location) {
        return privateMap.remove(location);
    }

    @Override
    public @Nullable Object get(@NotNull String expected) {
        return privateMap.get(expected);
    }

    @Override
    public int size() {
        return privateMap.size();
    }

    @Override
    public void clear() {
        privateMap.clear();
    }

    @Override
    public @NotNull Set<String> keySet() {
        return privateMap.keySet();
    }

    @Override
    public @NotNull Object getOrDefault(@NotNull Object key , @NotNull Object defaultObjectalue) {
        return privateMap.getOrDefault(key, defaultObjectalue);
    }

    @Override
    public @NotNull String toString() {
        return "[Map: "+privateMap.toString()+"]";
    }


    public @NotNull ConcurrentMap<String, Object> getMap() {
        return new ConcurrentHashMap<>(privateMap);
    }

    public boolean containsElements(@NotNull final FlowMap expectedOutput) {
        for (String key : expectedOutput.keySet()) {
            if(!compareElement(get(key), expectedOutput.get(key))) return false;
        }

        return true;
    }

    @NotNull @Override public FlowHistory getFlowHistory() { return flowHistory; }

    @Override public UUID getId() { return uuid; }

    @Override
    public <A> A eval(T<A> template) {
        return Template.eval(template, this);
    }

    @Override
    public <A> A evalOrDefault(T<A> template, A object) {
        try {
            A eval = this.eval(template);
            if(eval != null) return eval;
        } catch (Exception ignore) {}

        return object;
    }

    @Override
    public <A> A evalIdentity(T<A> t) {
        return Template.eval(t, new IdentityFlowMap());
    }

    @Override
    public <A> A input(T<A> template) {
        return eval(template);
    }

    @Override
    public <A> void output(T<A> locationAndType, A object) {
        // for now output templates are only strings
        String json = locationAndType.getRawJson();
        put(json, object);
    }

    private boolean descendMap(@NotNull final Map<?,?> currentMap, @NotNull final Map<?,?> otherMap) {
        for (Object s : otherMap.keySet()) {
            Object otherElement = otherMap.get(s);
            if(otherElement == null) return false;

            Object thisElement = currentMap.get(s);

            if(!compareElement(thisElement, otherElement)) {
                return false;
            }
        }

        return true;
    }

    private boolean compareElement(@Nullable final Object thisElement, @Nullable final Object otherElement) {
        if(thisElement == null || otherElement == null) return false;

        if(Map.class.isAssignableFrom(thisElement.getClass()) && Map.class.isAssignableFrom(otherElement.getClass())) {
            return descendMap((Map<?, ?>) thisElement, (Map<?, ?>) otherElement);
        } else
        if(Collection.class.isAssignableFrom(thisElement.getClass()) && Collection.class.isAssignableFrom(otherElement.getClass())) {
            return descendCollection((Collection<?>) thisElement, (Collection<?>) otherElement);
        } else {
            return thisElement.equals(otherElement);
        }

    }

    private boolean descendCollection(@NotNull final Collection<?> currentCollection, @NotNull final Collection<?> otherCollection) {
        // if empty, current collection should also be empty
        if(otherCollection.isEmpty() && !currentCollection.isEmpty()) return false;

        for (Object otherObject : otherCollection) {
            boolean foundAtLeastOneMatch = false;

            for (Object currentObject : currentCollection) {
                foundAtLeastOneMatch = compareElement(currentObject, otherObject);
                if(foundAtLeastOneMatch) break;
            }

            if (!foundAtLeastOneMatch) return false;

        }

        return true;
    }

    public static synchronized @NotNull FlowMapImpl of(final @NotNull Map<String, Object> initialArguments) {
        return new FlowMapImpl(new ConcurrentHashMap<>(Objects.requireNonNullElseGet(initialArguments, Map::of)));
    }

    public static synchronized @NotNull FlowMapImpl copy(final @NotNull FlowMapImpl o) {
        return new FlowMapImpl( new ConcurrentHashMap<>(o.privateMap) );
    }

    public static synchronized @NotNull FlowMapImpl copy(final @NotNull FlowMap o) {
        return new FlowMapImpl( new ConcurrentHashMap<>(((FlowMapImpl) o).privateMap) );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowMapImpl flowMap = (FlowMapImpl) o;
        return privateMap.equals(flowMap.privateMap);
//                &&
//                flowState.equals(flowMap.flowState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(privateMap);
    }
}
