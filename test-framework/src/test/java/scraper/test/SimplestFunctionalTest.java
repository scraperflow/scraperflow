package scraper.test;

import org.junit.jupiter.api.Test;
import scraper.nodes.test.Put;
import scraper.nodes.test.Simplest;

import java.util.List;
import java.util.Map;

import static scraper.test.FunctionalTest.runWith;

public class SimplestFunctionalTest {

    @Test
    public void dummyTest() throws Exception {
        runWith(Simplest.class, List.of(
                // node configuration
                Map.of(),
                // input
                Map.of("keyToBeDeleted", "notnull"),
                // output
                Map.of()
        ));
    }

    @Test
    public void putTest() throws Exception {
        runWith(Put.class, List.of(
                // node configuration
                Map.of("toPut", "here"),
                // input
                Map.of("ok", "notnull"),
                // output
                Map.of("hello", "world", "here", "result")
        ));
    }
}

