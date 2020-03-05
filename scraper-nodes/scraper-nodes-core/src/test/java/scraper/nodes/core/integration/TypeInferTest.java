package scraper.nodes.core.integration;

import junitparams.JUnitParamsRunner;
import org.junit.runner.RunWith;
import scraper.nodes.core.test.annotations.Workflow;
import scraper.nodes.core.test.helper.WorkflowTest;

@RunWith(JUnitParamsRunner.class)
public class TypeInferTest extends WorkflowTest {
    @Workflow("validListFlatten.yf")
    public void validFlatten() {}

    @Workflow("invalidListFlatten.yf")
    public void invalidFlatten() {}
}

