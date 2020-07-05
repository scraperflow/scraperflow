package scraper.nodes.core.flow;

import org.junit.jupiter.api.Test;
import scraper.api.exceptions.ValidationException;

import static scraper.test.WorkflowTest.resource;
import static scraper.test.WorkflowTest.runWith;


public class PipeTest {

    @Test
    public void simplePipe() {
        runWith(resource(PipeTest.class, "pipe/pipe-simple.jf"));
    }

    @Test
    public void missingTarget() {
        runWith(resource(PipeTest.class, "pipe/pipe-missing-target.jf"), ValidationException.class);
    }
}

