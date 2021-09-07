package scraper.nodes.core.functional;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static scraper.test.FunctionalTest.runWith;

public class PadTest {

    @Test
    public void basic() {
        runWith(Pad.class, List.of(
                Map.of("value", "1", "pad", 1, "output", "output" ),
                Map.of(),
                Map.of("output","1")
        ));
        runWith(Pad.class, List.of(
                Map.of("value", "12", "pad", 1, "output", "output" ),
                Map.of(),
                Map.of("output","12")
        ));
    }

    @Test
    public void pad() {
        runWith(Pad.class, List.of(
                Map.of("value", "1", "pad", 2, "output", "output" ),
                Map.of(),
                Map.of("output","01")
        ));
        runWith(Pad.class, List.of(
                Map.of("value", "12", "pad", 4, "output", "output" ),
                Map.of(),
                Map.of("output","0012")
        ));
    }
}

