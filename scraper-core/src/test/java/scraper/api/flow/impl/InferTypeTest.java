package scraper.api.flow.impl;

import com.google.common.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class InferTypeTest {

    @Test
    public void inferObjectTest() {
        Object o = new Object();
        TypeToken<?> inferred = FlowMapImpl.inferType(o);

        Assert.assertEquals(inferred, TypeToken.of(Object.class));
    }

    @Test
    public void stringTest() {
        String o = "";
        TypeToken<?> inferred = FlowMapImpl.inferType(o);

        Assert.assertEquals(inferred, TypeToken.of(String.class));
    }

    @Test
    public void mapTest() {
        Map<String, Integer> o = Map.of("123",1,"42",42);
        TypeToken<?> inferred = FlowMapImpl.inferType(o);

        Assert.assertEquals(inferred, new TypeToken<Map<String, Integer>>(){});
    }

    @Test
    public void complexTest() {
        Object o = List.of("a", "b", Map.of("a", List.of("1", "2")), "d");
        TypeToken<?> inferred = FlowMapImpl.inferType(o);
        Assert.assertEquals(new TypeToken<List<Object>>(){}, inferred);
    }
}