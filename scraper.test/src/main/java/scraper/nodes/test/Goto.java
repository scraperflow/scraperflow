package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.NodePlugin;
import scraper.api.NodeException;
import scraper.api.FlowMap;
import scraper.api.NodeContainer;
import scraper.api.Node;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class Goto implements Node {

    @NotNull
    @Override
    public void process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException { }
}