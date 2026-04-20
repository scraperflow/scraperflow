package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.NodePlugin;
import scraper.api.NodeException;
import scraper.api.FlowMap;
import scraper.api.NodeContainer;
import scraper.api.Node;

import static scraper.api.NodeLogLevel.*;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class SimpleLog implements Node {

    @NotNull
    @Override
    public void process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {
        n.log(TRACE, "1");
        n.log(DEBUG, "2");
        n.log(INFO, "3");
        n.log(WARN, "4");
        n.log(ERROR, "5");
    }
}