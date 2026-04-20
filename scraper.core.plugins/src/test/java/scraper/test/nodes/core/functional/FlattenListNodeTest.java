package scraper.test.nodes.core.functional;

import org.junit.jupiter.api.Test;
import scraper.nodes.core.functional.FlattenList;

import java.util.List;
import java.util.Map;

import static scraper.test.FunctionalTest.runWith;

public class FlattenListNodeTest {

    @Test
    public void emptyLists() {
        runWith(FlattenList.class, List.of(
                Map.of("output", "output","flatten", List.of(List.of(), List.of())),
                Map.of(),
                Map.of("output",List.of())
        ));
    }

    @Test
    public void singletonList() {
        runWith(FlattenList .class, List.of(
                Map.of("output", "output","flatten", List.of(List.of(), List.of(50))),
                Map.of(),
                Map.of("output",List.of(50))
        ));
    }

    @Test
    public void simpleLists() {
        runWith(FlattenList .class, List.of(
                Map.of("output", "output", "flatten", List.of(List.of("123120"), List.of("qweq"))),
                Map.of(),
                Map.of("output",List.of("123120","qweq"))
        ));
    }

    @Test
    public void badTypes() {
        runWith(FlattenList .class, List.of(
                Map.of("output", "output","flatten", List.of(List.of("wontwork"), List.of(50))),
                Map.of(),
                Map.of()
        ));
    }
}

