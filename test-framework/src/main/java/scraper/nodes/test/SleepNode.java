package scraper.nodes.test;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;

@NodePlugin
public class SleepNode implements Node {
    private @FlowKey(mandatory = true) Integer sleep;

    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return n.forward(o);
    }
}
