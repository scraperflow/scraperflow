package scraper.nodes.core.functional;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static scraper.test.FunctionalTest.runWith;

public class ListSortTest {

    @Test
    public void basicSort() {
        runWith(ListSort.class, List.of(
                Map.of("list", List.of("123", "12333","99","1"), "output","output"),
                Map.of(),
                Map.of("output",List.of("1","99","123","12333"))
        ));
    }

    @Test
    public void basicSortInts() {
        runWith(ListSort.class, List.of(
                Map.of("list", List.of(123, 12333,99,1), "output","output"),
                Map.of(),
                Map.of("output",List.of(1,99,123,12333))
        ));
    }

    @Test
    public void sort() {
        runWith(ListSort.class, List.of(
                Map.of("list", List.of("123 hello", "12333 world","99 (ok)","1 (no)"), "output","output"),
                Map.of(),
                Map.of("output",List.of("1 (no)","99 (ok)","123 hello","12333 world"))
        ));
    }
}

