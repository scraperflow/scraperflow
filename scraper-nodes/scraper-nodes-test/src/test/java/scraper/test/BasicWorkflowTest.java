package scraper.test;

import junitparams.JUnitParamsRunner;
import org.junit.runner.RunWith;
import scraper.nodes.test.annotations.Workflow;
import scraper.nodes.test.helper.WorkflowTest;


@RunWith(JUnitParamsRunner.class)
public class BasicWorkflowTest extends WorkflowTest {

    @Workflow("simple.jf")
    public void basic() {}

    @Workflow("goTo.jf")
    public void goToTest() {}

    @Workflow("import-simple.jf")
    public void importTest() {}
}
