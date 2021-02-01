package scraper.test;

import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.nodes.test.Assert;

import java.util.List;

public class TestUtil {
    public static void assertSuccess(List<NodeContainer<? extends Node>> jobProcess) {
        for (NodeContainer<? extends Node> process : jobProcess) {
            Node container = process.getC();

            if(Assert.class.isAssignableFrom(container.getClass())) {
                Assert assertNode = (Assert) container;

                if(assertNode.isFinished()) {
                    if(!assertNode.getSuccess().get())
                        throw new IllegalStateException("failed "+process.getAddress());
                } else {
                    synchronized (assertNode.getSuccess()) {
                        try {
                            assertNode.getSuccess().wait(1000);
                            if(!assertNode.getSuccess().get())
                                throw new IllegalStateException("failed "+process.getAddress());
                        } catch (InterruptedException e) {
                            throw new IllegalStateException("failed "+e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
