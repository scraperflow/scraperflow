package scraper.test.nodes.core.flow;

import org.junit.jupiter.api.Test;
import scraper.utils.ClassUtil;

import static scraper.test.WorkflowTest.runWith;


public class MapTest {

    @Test
    public void simpleMap() {
        runWith(ClassUtil.getResourceUrl(MapTest.class, "map/simple.jf"), IllegalStateException.class);
    }

}

