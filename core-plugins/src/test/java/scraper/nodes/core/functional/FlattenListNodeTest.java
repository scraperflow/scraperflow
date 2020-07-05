package scraper.nodes.core.functional;

import org.junit.jupiter.api.Test;
import scraper.test.FunctionalTest;

import java.util.List;
import java.util.Map;

import static scraper.test.FunctionalTest.runWith;

public class FlattenListNodeTest {

    @Test
    public void emptyLists() {
        runWith(FlattenListNode.class, List.of(
                Map.of("output", "output","flatten", List.of(List.of(), List.of())),
                Map.of(),
                Map.of("output",List.of())
        ));
    }

    @Test
    public void singletonList() {
        runWith(FlattenListNode.class, List.of(
                Map.of("output", "output","flatten", List.of(List.of(), List.of(50))),
                Map.of(),
                Map.of("output",List.of(50))
        ));
    }

    @Test
    public void simpleLists() {
        runWith(FlattenListNode.class, List.of(
                Map.of("output", "output", "flatten", List.of(List.of("123120"), List.of("qweq"))),
                Map.of(),
                Map.of("output",List.of("123120","qweq"))
        ));
    }

    @Test
    public void badTypes() {
        runWith(FlattenListNode.class, List.of(
                Map.of("output", "output","flatten", List.of(List.of("wontwork"), List.of(50))),
                Map.of(),
                Map.of()
        ));
    }
}

