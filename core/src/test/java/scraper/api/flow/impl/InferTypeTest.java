package scraper.api.flow.impl;

import org.junit.jupiter.api.Test;
import scraper.api.T;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InferTypeTest {

    @Test
    public void inferObjectTest() {
        Object o = new Object();
        T<?> inferred = FlowMapImpl.inferType(o);

        assertTrue(inferred.equalsType(new T<>(){}));
    }

    @Test
    public void stringTest() {
        String o = "";
        T<?> inferred = FlowMapImpl.inferType(o);

        assertTrue(inferred.equalsType(new T<String>(){}));
    }

    @Test
    public void mapTest() {
        Map<String, Integer> o = Map.of("123",1,"42",42);
        T<?> inferred = FlowMapImpl.inferType(o);

        assertTrue(inferred.equalsType(new T<Map<String, Integer>>(){}));
    }

    @Test
    public void complexTest() {
        Object o = List.of("a", "b", Map.of("a", List.of("1", "2")), "d");
        T<?> inferred = FlowMapImpl.inferType(o);
        assertTrue(new T<List<Object>>(){}.equalsType(inferred));
    }
}