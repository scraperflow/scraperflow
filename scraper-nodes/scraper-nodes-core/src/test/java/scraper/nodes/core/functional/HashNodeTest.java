package scraper.nodes.core.functional;

import junitparams.JUnitParamsRunner;
import org.junit.runner.RunWith;
import scraper.nodes.core.test.annotations.Functional;
import scraper.nodes.core.test.helper.FunctionalTest;

import java.util.List;
import java.util.Map;

@RunWith(JUnitParamsRunner.class)
public class HashNodeTest extends FunctionalTest {
    @Functional(HashNode.class)
    public Object[] defaultOutputTest() {
        int hash = "hello".hashCode();
        return new Object[]{
                Map.of("content", "hello"),
                Map.of("output", "notahash"),
                Map.of("output", String.valueOf(hash))
        };
    }
}

