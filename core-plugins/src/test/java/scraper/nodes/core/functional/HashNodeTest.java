package scraper.nodes.core.functional;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static scraper.test.FunctionalTest.runWith;

public class HashNodeTest {

    @Test
    public void defaultOutputTest() {
        int hash = "hello".hashCode();

        runWith(HashNode.class, List.of(
                Map.of("content", "hello"),
                Map.of("output", "notahash"),
                Map.of("output", String.valueOf(hash))
        ));
    }
}

