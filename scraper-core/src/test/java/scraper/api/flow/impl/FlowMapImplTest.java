package scraper.api.flow.impl;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.flow.FlowMap;
import scraper.api.template.L;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.*;

public class FlowMapImplTest {

    public static Object staticObject = new Object();

    @Test
    public void mapFunctionsTest() {
        // direct instantiation
        FlowMap o = FlowMapImpl.origin();
        Assert.assertNotNull(o.toString());

        // put and get
        o.output("key1", 123);
        Assert.assertTrue(o.get("key1").isPresent());
        Assert.assertEquals(123, o.get("key1").get());

        // clear
        Assert.assertEquals(1, o.size());
        o.clear();
        Assert.assertEquals(0, o.size());

        // putAll
        Map.of("k1", 42, "k2", "hello").forEach(o::output);
        Assert.assertTrue(o.get("k1").isPresent());
        Assert.assertTrue(o.get("k2").isPresent());
        Assert.assertEquals(42, o.get("k1").get());
        Assert.assertEquals("hello", o.get("k2").get());
        Assert.assertEquals(2, o.size());
        assertTrue(o.keySet().contains("k1"));
        assertTrue(o.keySet().contains("k2"));

        // remove
        o.remove("k2");
        Assert.assertTrue(o.get("k2").isEmpty());
        Assert.assertEquals(1, o.size());
    }

    @Test
    public void flowEqualityTest() {
        ConcurrentMap<String, Object> initialMap = new ConcurrentHashMap<>();
        initialMap.put("o", staticObject);
        initialMap.put("answer", 42);

        // direct instantiation
        FlowMap flowMap = FlowMapImpl.origin(initialMap);

        // copy
        FlowMap flowMap1 = flowMap.copy();
        FlowMap flowMap2 = flowMap.copy();
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
        FlowMap o1 = FlowMapImpl.origin(Map.of("k1",123, "k2", 42));
        FlowMap o2 = FlowMapImpl.origin(Map.of("k1",123, "k2", 42, "more", "more!"));
        FlowMap o3 = FlowMapImpl.origin(Map.of("k1",123, "k2", 42, "more", "more!", "even more", "ok"));

        // reflexivity
        Assert.assertTrue(o1.containsElements(o1));

        // anti-symmetry
        Assert.assertTrue(o2.containsElements(o1));
        Assert.assertFalse(o1.containsElements(o2));

        // transitivity
        Assert.assertTrue(o2.containsElements(o1));
        Assert.assertTrue(o3.containsElements(o2));
        Assert.assertTrue(o3.containsElements(o1));
    }

    @Test
    public void flowContainsDescendCollectionsTest() {
        FlowMap o1 = FlowMapImpl.origin(Map.of("k1",123, "k2", List.of(Map.of("1",1), "ok", List.of("123", "123"))));
        FlowMap o2 = FlowMapImpl.origin(Map.of("k1",123, "k2", List.of(Map.of("1",1,"2","2"), "moreElementsInTheSet", "ok", List.of("listIsBigger","123", "123"))));

        // o2 has at least all elements of o1
        Assert.assertTrue(o2.containsElements(o1));
        Assert.assertFalse(o1.containsElements(o2));

        assertNotEquals(o1.hashCode(), o2.hashCode());
    }

    @Test
    public <V> void getWithTypeTest() {
        FlowMap o1 = FlowMapImpl.origin(Map.of("k1",123, "k2", List.of(Map.of("1",1), "ok", List.of("123", "123"))));

        Optional<List<V>> tt = o1.getWithType("k2", new L<>(){}.get());
        Assert.assertTrue(tt.isPresent());
    }

}