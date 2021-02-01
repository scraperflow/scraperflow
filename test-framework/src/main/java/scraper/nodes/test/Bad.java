package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;

@NodePlugin(value = "0.1.0")
public final class Bad implements FunctionalNode {
    public Bad() { throw new RuntimeException(); }

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) {}
}