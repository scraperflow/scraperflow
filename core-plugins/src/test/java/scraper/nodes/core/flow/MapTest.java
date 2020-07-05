package scraper.nodes.core.flow;

import org.junit.jupiter.api.Test;

import static scraper.test.WorkflowTest.resource;
import static scraper.test.WorkflowTest.runWith;


public class MapTest {

    @Test
    public void simpleMap() {
        runWith(resource(MapTest.class, "map/simple.jf"), IllegalStateException.class);
    }

    @Test
    public void simpleException() {
        runWith(resource(MapTest.class, "map/exception.jf"), IllegalStateException.class);
    }
}

