package scraper.test.nodes.core.functional;

import org.junit.jupiter.api.Test;
import scraper.nodes.core.functional.Hash;

import java.util.List;
import java.util.Map;

import static scraper.test.FunctionalTest.runWith;

public class HashNodeTest {

    @Test
    public void defaultOutputTest() {
        int hash = "hello".hashCode();

        runWith(Hash.class, List.of(
                Map.of("content", "hello", "output", "output"),
                Map.of("output", "notahash"),
                Map.of("output", String.valueOf(hash))
        ));
    }
}

