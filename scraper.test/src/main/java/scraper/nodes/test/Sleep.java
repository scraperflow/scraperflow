package scraper.nodes.test;


import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.NodeException;
import scraper.api.FlowMap;
import scraper.api.NodeContainer;
import scraper.api.Node;

@NodePlugin
public class Sleep implements Node {
    private @FlowKey(mandatory = true) Integer sleep;

    @NotNull
    @Override
    public void process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
