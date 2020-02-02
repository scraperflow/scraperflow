package scraper.nodes.core.test.helper;

import org.junit.Assert;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.nodes.core.test.addons.AssertNode;

import java.util.List;

public class TestUtil {
    public static void assertSuccess(List<NodeContainer<? extends Node>> jobProcess) {
        for (NodeContainer<? extends Node> process : jobProcess) {
            if(AssertNode.class.isAssignableFrom(process.getC().getClass())) {
                if(((AssertNode) process.getC()).isFinished()) {
                    Assert.assertTrue("failed "+process.getAddress(),
                            ((AssertNode) process.getC()).getSuccess().get());
                } else {
                    synchronized (((AssertNode) process.getC()).getSuccess()) {
                        try {
                            ((AssertNode) process.getC()).getSuccess().wait(1000);
                            Assert.assertTrue(((AssertNode) process.getC()).getSuccess().get());
                        } catch (InterruptedException e) {
                            Assert.fail(e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
