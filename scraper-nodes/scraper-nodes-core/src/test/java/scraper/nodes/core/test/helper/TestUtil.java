package scraper.nodes.core.test.helper;

import org.junit.Assert;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.nodes.core.test.addons.AssertNode;

import java.util.List;

public class TestUtil {
    public static void assertSuccess(List<NodeContainer<? extends Node>> jobProcess) {
        for (NodeContainer<? extends Node> process : jobProcess) {
            Node container = process.getC();

            if(AssertNode.class.isAssignableFrom(container.getClass())) {
                AssertNode assertNode = (AssertNode) container;

                if(assertNode.isFinished()) {
                    Assert.assertTrue("failed "+process.getAddress(), assertNode.getSuccess().get());
                } else {
                    synchronized (assertNode.getSuccess()) {
                        try {
                            assertNode.getSuccess().wait(1000);
                            Assert.assertTrue(assertNode.getSuccess().get());
                        } catch (InterruptedException e) {
                            Assert.fail(e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
