package scraper.test;

import org.junit.jupiter.api.Test;
import scraper.nodes.test.PutNode;
import scraper.nodes.test.SimplestNode;

import java.util.List;
import java.util.Map;

import static scraper.test.FunctionalTest.runWith;

public class SimplestFunctionalTest {

    @Test
    public void dummyTest() throws Exception {
        runWith(SimplestNode.class, List.of(
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
        runWith(PutNode.class, List.of(
                // node configuration
                Map.of("toPut", "here"),
                // input
                Map.of("ok", "notnull"),
                // output
                Map.of("hello", "world", "here", "result")
        ));
    }
}

