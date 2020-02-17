package scraper.nodes.core.flow;

import junitparams.JUnitParamsRunner;
import org.junit.runner.RunWith;
import scraper.api.exceptions.ValidationException;
import scraper.nodes.core.test.annotations.Workflow;
import scraper.nodes.core.test.helper.WorkflowTest;


@RunWith(JUnitParamsRunner.class)
public class PipeTest extends WorkflowTest {

    @Workflow("pipe-simple.jf")
    public void simplePipe() {}

    @Workflow(value = "pipe-missing-target.jf", expectToFail = ValidationException.class)
    public void missingTarget() {}
}

