package scraper.core;

import org.junit.Before;
import org.junit.Test;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.util.NodeUtil;

import java.util.HashMap;


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
}