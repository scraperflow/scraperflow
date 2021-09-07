package scraper.nodes.test;


import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.NodeException;
import scraper.api.FlowMap;
import scraper.api.NodeContainer;
import scraper.api.NodeLogLevel;
import scraper.api.Node;

@NodePlugin(deprecated = true)
public class Exception implements Node {

    private @FlowKey(mandatory = true) String exception;

    @NotNull
    @Override
    public void process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {
        switch (exception) {
            case "NODE": {
                throw new NodeException("Dummy Exception at " + n.getAddress());
            }
            default:
                n.log(NodeLogLevel.ERROR, "Failing test of " + n.getJobInstance().getName());
                throw new AssertionError("Failed test: " + exception);
        }
    }
}
