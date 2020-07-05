package scraper.nodes.core.flow;

import org.junit.jupiter.api.Test;

import static scraper.test.WorkflowTest.resource;
import static scraper.test.WorkflowTest.runWith;


public class MapJoinTest {

    @Test
    public void simpleMapJoin() {
        runWith(resource(MapJoinTest.class, "mapjoin/simple.yf"));
    }
}

