package scraper.api.flow.impl;

import scraper.annotations.NotNull;
import scraper.api.flow.FlowHistory;
import scraper.api.flow.FlowMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FlowMapImpl implements FlowMap {

    private final ConcurrentMap<String, Object> privateMap;

    private final FlowHistory flowHistory = new FlowHistoryImpl();

    public FlowMapImpl(@NotNull ConcurrentMap<String, Object> privateMap) { this.privateMap = privateMap; }

    public FlowMapImpl() { privateMap = new ConcurrentHashMap<>(); }

    @Override
    public Object put(@NotNull String location, @NotNull Object value) {
        return privateMap.put(location, value);
    }

    @Override
    public void putAll(@NotNull Map<String, Object> m) {
        privateMap.putAll(m);
    }

    @Override
    public Object remove(@NotNull String location) {
        return privateMap.remove(location);
    }

    @Override
    public Object get(@NotNull String expected) {
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

    @NotNull
    @Override
    public Set<String> keySet() {
        return privateMap.keySet();
    }

    @NotNull
    @Override
    public Object getOrDefault(@NotNull Object key , @NotNull Object defaultObjectalue) {
        return privateMap.getOrDefault(key, defaultObjectalue);
    }

    @Override
    public String toString() {
        return "[Map: "+privateMap.toString()+"]";
    }


    public ConcurrentMap<String, Object> getMap() {
        return new ConcurrentHashMap<>(privateMap);
    }

    public boolean containsElements(@NotNull FlowMap expectedOutput) {
        for (String key : expectedOutput.keySet()) {
            if(!compareElement(get(key), expectedOutput.get(key))) return false;
        }

        return true;
    }

    @NotNull @Override public FlowHistory getFlowHistory() { return flowHistory; }

    private boolean descendMap(Map<?,?> currentMap, Map<?,?> otherMap) {
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

    private boolean compareElement(Object thisElement, Object otherElement) {
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

    private boolean descendCollection(Collection<?> currentCollection, Collection<?> otherCollection) {
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

    public static synchronized FlowMapImpl of(Map<String, Object> initialArguments) {
        if(initialArguments == null) initialArguments = new HashMap<>();
        return new FlowMapImpl(new ConcurrentHashMap<>(initialArguments));
    }

    public static synchronized FlowMapImpl copy(FlowMapImpl o) {
        return new FlowMapImpl(
                new ConcurrentHashMap<>(o.privateMap)
        );
    }

    public static synchronized FlowMapImpl copy(FlowMap o) {
        return new FlowMapImpl(
                new ConcurrentHashMap<>(((FlowMapImpl) o).privateMap)
        );
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
