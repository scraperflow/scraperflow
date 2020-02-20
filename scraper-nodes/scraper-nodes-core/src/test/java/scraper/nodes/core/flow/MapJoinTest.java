package scraper.nodes.core.flow;

import junitparams.JUnitParamsRunner;
import org.junit.runner.RunWith;
import scraper.nodes.core.test.annotations.Workflow;
import scraper.nodes.core.test.helper.WorkflowTest;


@RunWith(JUnitParamsRunner.class)
public class MapJoinTest extends WorkflowTest {

    @Workflow("simple.yf")
    public void simpleMapJoin() {}
}

