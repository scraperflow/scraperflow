package scraper.api.flow.impl;

import org.junit.jupiter.api.Test;
import scraper.api.FlowMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.*;

public class FlowMapImplTest {

    public static Object staticObject = new Object();

    @Test
    public void mapFunctionsTest() {
        // direct instantiation
        FlowMapImpl o = (FlowMapImpl) FlowMapImpl.origin();
        assertNotNull(o.toString());

        // put and get
        o.output("key1", 123);
        assertNotNull(o.getPrivateMap().get("key1"));
        assertEquals(123, o.getPrivateMap().get("key1"));

        // clear
        assertEquals(1, o.getPrivateMap().size());
        o.clear();
        assertEquals(0, o.getPrivateMap().size());

        // putAll
        Map.of("k1", 42, "k2", "hello").forEach(o::output);
        assertNotNull(o.getPrivateMap().get("k1"));
        assertNotNull(o.getPrivateMap().get("k2"));
        assertEquals(42, o.getPrivateMap().get("k1"));
        assertEquals("hello", o.getPrivateMap().get("k2"));
        assertEquals(2, o.getPrivateMap().size());
        assertTrue(o.getPrivateMap().containsKey("k1"));
        assertTrue(o.getPrivateMap().containsKey("k2"));

        // remove
        o.remove("k2");
        assertNull(o.getPrivateMap().get("k2"));
        assertEquals(1, o.getPrivateMap().size());
    }

    @Test
    public void flowEqualityTest() {
        ConcurrentMap<String, Object> initialMap = new ConcurrentHashMap<>();
        initialMap.put("o", staticObject);
        initialMap.put("answer", 42);

        // direct instantiation
        FlowMap flowMap = FlowMapImpl.origin(initialMap);

        // copy
        FlowMapImpl flowMap1 = (FlowMapImpl) flowMap.copy();
        FlowMapImpl flowMap2 = (FlowMapImpl) flowMap.copy();
        FlowMap flowMap3 = flowMap.copy();

        assertEquals(flowMap, flowMap1);
        assertNotSame(flowMap, flowMap1);

        assertEquals(flowMap, flowMap2);
        assertNotSame(flowMap, flowMap2);

        assertEquals(flowMap, flowMap3);
        assertNotSame(flowMap, flowMap3);


        // equals even though different (map) objects are used

        Map<String, Object> mapInMap1 = new HashMap<>();
        Map<String, Object> mapInMap2 = new HashMap<>();

        mapInMap1.put("key1", "yes");
        mapInMap2.put("key1", "yes");

        flowMap1.output("map", mapInMap1);
        flowMap2.output("map", mapInMap2);

        assertEquals(flowMap1, flowMap2);
        assertNotSame(flowMap1, flowMap2);

        // not equal anymore

        mapInMap1.clear();
        assertNotEquals(flowMap1, flowMap2);
        assertNotSame(flowMap1, flowMap2);
    }

    @Test
    public void flowContainsTest() {
        FlowMapImpl o1 = (FlowMapImpl) FlowMapImpl.origin(Map.of("k1",123, "k2", 42));
        FlowMapImpl o2 = (FlowMapImpl) FlowMapImpl.origin(Map.of("k1",123, "k2", 42, "more", "more!"));
        FlowMapImpl o3 = (FlowMapImpl) FlowMapImpl.origin(Map.of("k1",123, "k2", 42, "more", "more!", "even more", "ok"));

        // reflexivity
        assertTrue(o1.containsElements(o1));

        // anti-symmetry
        assertTrue(o2.containsElements(o1));
        assertFalse(o1.containsElements(o2));

        // transitivity
        assertTrue(o2.containsElements(o1));
        assertTrue(o3.containsElements(o2));
        assertTrue(o3.containsElements(o1));
    }

    @Test
    public void flowContainsDescendCollectionsTest() {
        FlowMapImpl o1 = (FlowMapImpl) FlowMapImpl.origin(Map.of("k1",123, "k2", List.of(Map.of("1",1), "ok", List.of("123", "123"))));
        FlowMapImpl o2 = (FlowMapImpl) FlowMapImpl.origin(Map.of("k1",123, "k2", List.of(Map.of("1",1,"2","2"), "moreElementsInTheSet", "ok", List.of("listIsBigger","123", "123"))));

        // o2 has at least all elements of o1
        assertTrue(o2.containsElements(o1));
        assertFalse(o1.containsElements(o2));

        assertNotEquals(o1.hashCode(), o2.hashCode());
    }

}