package scraper.nodes.core.functional;

import junitparams.JUnitParamsRunner;
import org.junit.runner.RunWith;
import scraper.nodes.core.test.annotations.Functional;
import scraper.nodes.core.test.helper.FunctionalTest;

import java.util.List;
import java.util.Map;

@RunWith(JUnitParamsRunner.class)
public class EchoNodeTest extends FunctionalTest {
    @Functional(EchoNode.class)
    public Object[] putTest() {
        return new Object[]{
                Map.of("put", "ok", "value", "hello"),
                Map.of(),
                Map.of("ok","hello")
        };
    }
}

