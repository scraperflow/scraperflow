package scraper.nodes.core.flow;

import junitparams.JUnitParamsRunner;
import org.junit.runner.RunWith;
import scraper.nodes.core.test.annotations.Workflow;
import scraper.nodes.core.test.helper.WorkflowTest;


@RunWith(JUnitParamsRunner.class)
public class MapTest extends WorkflowTest {

    @Workflow("simple.jf")
    public void simpleMap() {}
}

