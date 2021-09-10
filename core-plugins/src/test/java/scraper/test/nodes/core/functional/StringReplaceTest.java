package scraper.test.nodes.core.functional;

import org.junit.jupiter.api.Test;
import scraper.nodes.core.functional.StringReplace;

import java.util.List;
import java.util.Map;

import static scraper.test.FunctionalTest.runWith;

public class StringReplaceTest {

    @Test
    public void putTest() {
        runWith(StringReplace.class, List.of(
                Map.of("replace", ",", "with", "","content", "9,1234","output","output"),
                Map.of(),
                Map.of("output","91234")
        ));
    }
}

