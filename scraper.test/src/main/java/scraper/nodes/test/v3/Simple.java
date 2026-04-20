package scraper.nodes.test.v3;

import scraper.annotations.NotNull;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.NodeContainer;
import scraper.api.Node;

@NodePlugin(value = "0.9.0", deprecated = true)
public final class Simple implements Node {
    @NotNull @Override public void process(@NotNull NodeContainer<? extends Node> n, @NotNull final FlowMap o) { }
}