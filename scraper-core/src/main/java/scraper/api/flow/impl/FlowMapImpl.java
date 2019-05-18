package scraper.api.flow.impl;

import scraper.api.flow.FlowState;
import scraper.api.flow.FlowMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FlowMapImpl implements FlowMap {

    private final ConcurrentMap<String, Object> privateMap;

    private FlowState flowState = FlowStateImpl.initial();

    public FlowMapImpl(ConcurrentMap<String, Object> privateMap) {
        this.privateMap = privateMap;
    }

    public FlowMapImpl() {
        privateMap = new ConcurrentHashMap<>();
    }

    @Override
    public Object put(String location, Object value) {
        return privateMap.put(location, value);
    }

    @Override
    public void putAll(Map<String, Object> m) {
        privateMap.putAll(m);
    }

    @Override
    public Object remove(String location) {
        return privateMap.remove(location);
    }

    @Override
    public Object get(String expected) {
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
    public Set<String> keySet() {
        return privateMap.keySet();
    }

    @Override
    public Object getOrDefault(Object key , Object defaultObjectalue) {
        return privateMap.getOrDefault(key, defaultObjectalue);
    }

    @Override
    public String toString() {
        return "[Map: "+privateMap.toString()+"]";
    }


    public ConcurrentMap<String, Object> getMap() {
        return new ConcurrentHashMap<>(privateMap);
    }

    public boolean containsElements(FlowMap expectedOutput) {
        Map<String, Object> currentMap = getMap();
        Map<String, Object> otherMap = expectedOutput.getMap();

        return descendMap(currentMap, otherMap);
    }

    @Override
    public FlowState getFlowState() {
        return flowState;
    }

    @Override
    public void setFlowState(FlowState newState) {
        flowState = newState;
    }

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
            if(!descendMap((Map<?,?>) thisElement, (Map<?,?>) otherElement)) return false;
        } else
        if(Collection.class.isAssignableFrom(thisElement.getClass()) && Collection.class.isAssignableFrom(otherElement.getClass())) {
            if(!descendCollection((Collection<?>) thisElement, (Collection<?>) otherElement)) return false;
        } else {
            if (!thisElement.equals(otherElement)) return false;
        }

        return true;
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
        return privateMap.equals(flowMap.privateMap) &&
                flowState.equals(flowMap.flowState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(privateMap, flowState);
    }
}
