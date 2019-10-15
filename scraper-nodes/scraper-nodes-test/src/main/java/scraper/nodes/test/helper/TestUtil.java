package scraper.nodes.test.helper;

import org.junit.Assert;
import scraper.api.node.Node;
import scraper.nodes.test.AssertNode;

import java.util.List;

public class TestUtil {
    public static void assertSuccess(List<Node> jobProcess) {
        for (Node process : jobProcess) {
            if(AssertNode.class.isAssignableFrom(process.getClass())) {
                if(((AssertNode) process).isFinished()) {
                    Assert.assertTrue(" failed "+process.getGoTo(),
                            ((AssertNode) process).getSuccess().get());
                } else {
                    synchronized (((AssertNode) process).getSuccess()) {
                        try {
                            ((AssertNode) process).getSuccess().wait(1000);
                            Assert.assertTrue(((AssertNode) process).getSuccess().get());
                        } catch (InterruptedException e) {
                            Assert.fail(e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
