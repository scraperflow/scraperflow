package scraper.nodes.test;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;

@NodePlugin(deprecated = true)
public class ExceptionNode implements Node {

    private @FlowKey(mandatory = true) String exception;

    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {
        switch (exception) {
            case "NODE": {
                throw new NodeException("Dummy Exception");
            }
            case "FAIL": {
                System.setProperty("workflow.fail", "fail");
//                Assert.fail("Failed test.");
            }
            default:
                throw new RuntimeException("default runtime exception");
        }
    }
}
