package scraper.nodes.core.functional;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static scraper.test.FunctionalTest.runWith;

public class EchoNodeTest {

    @Test
    public void putTest() {
        runWith(Echo .class, List.of(
                Map.of("put", "ok", "value", "hello"),
                Map.of(),
                Map.of("ok","hello")
        ));
    }
}

