package scraper.addons.v2;

import scraper.annotations.NotNull;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;

@NodePlugin(value = "0.2.0", deprecated = true)
public final class SimpleNode implements Node {
    @NotNull @Override public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull final FlowMap o) { return o; }
}