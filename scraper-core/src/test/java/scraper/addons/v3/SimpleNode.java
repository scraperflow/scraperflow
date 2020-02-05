package scraper.addons.v3;

import scraper.annotations.NotNull;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;

@NodePlugin(value = "1.0.0", deprecated = true)
public final class SimpleNode implements Node {
    @NotNull @Override public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull final FlowMap o) { return o; }
}