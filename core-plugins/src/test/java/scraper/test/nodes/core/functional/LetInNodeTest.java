package scraper.test.nodes.core.functional;

import org.junit.jupiter.api.Test;
import scraper.nodes.core.complex.LetIn;

import java.util.List;
import java.util.Map;

import static scraper.test.FunctionalTest.runWith;

public class LetInNodeTest {

    @Test
    public void noop() {
        runWith(LetIn.class, List.of(
                Map.of("keys", List.of()),
                Map.of(),
                Map.of()
        ));
    }

    @Test
    public void keep() {
        runWith(LetIn .class, List.of(
                Map.of("keys", List.of("e")),
                Map.of("e", "ok"),
                Map.of("e", "ok")
        ));
    }

    @Test
    public void filter() {
        runWith(LetIn .class, List.of(
                Map.of("keys", List.of("e")),
                Map.of("e", "ok", "e2", "no"),
                Map.of("e", "ok")
        ));
    }

    @Test
    public void remove() {
        runWith(LetIn .class, List.of(
                Map.of("keys", List.of()),
                Map.of("e", "ok", "e2", "no"),
                Map.of()
        ));
    }
}

