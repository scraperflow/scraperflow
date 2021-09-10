package scraper.test.nodes.core.stream;

import org.junit.jupiter.api.Test;
import scraper.utils.ClassUtil;

import static scraper.test.WorkflowTest.runWith;


public class RegexTest {

    @Test
    public void simpleRegex() {
        runWith(ClassUtil.getResourceUrl(RegexTest.class, "simple.yf"));
    }

}

