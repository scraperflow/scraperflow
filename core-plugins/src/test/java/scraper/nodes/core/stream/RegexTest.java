package scraper.nodes.core.stream;

import org.junit.jupiter.api.Test;

import static scraper.test.WorkflowTest.resource;
import static scraper.test.WorkflowTest.runWith;


public class RegexTest {

    @Test
    public void simpleRegex() {
        runWith(resource(RegexTest.class, "simple.yf"));
    }

}

