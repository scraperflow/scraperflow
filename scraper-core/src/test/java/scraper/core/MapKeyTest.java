package scraper.core;

import com.google.common.reflect.TypeToken;
import org.junit.Before;
import org.junit.Test;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.TypesafeObject;
import scraper.util.NodeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class MapKeyTest {

    private final static FlowMap o = NodeUtil.flowOf(new HashMap<>());

    @Before
    public void clean() { o.clear(); }

    @Test(expected = NodeException.class)
    public void missingKeyTest() throws NodeException {
        MapKey<String> missing = new MapKey<String>() {}.failOnMissing();
        missing.key = "goTo";
        missing.eval(o);
    }

    @Test(expected = NodeException.class)
    public void unexpectedRawTypeTest() throws NodeException {
        MapKey<String> missing = new MapKey<String>() {}.failOnMissing();
        missing.key = "goTo";
        o.put("goTo", 1);
        missing.eval(o);
    }

    @Test
    public void continueOnUnsafeTypeTest() throws NodeException {
        MapKey<List<String>> missing = new MapKey<List<String>>(){}.base(TypesafeAggregateStringList::new);
        missing.key = "goTo";
        o.put("goTo", new ArrayList<String>());

        System.setProperty("scraper.failOnNonSafeMapKey", "false");
        missing.eval(o);
        System.clearProperty("scraper.failOnNonSafeMapKey");
    }

    private class TypesafeAggregateStringList extends ArrayList<String> implements TypesafeObject {
        @Override public TypeToken<?> getType() { return new TypeToken<List<String>>(){}; }
    }
}