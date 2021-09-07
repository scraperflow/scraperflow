package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;

@NodePlugin(value = "0.1.0")
public final class Bad implements FunctionalNode {
    public Bad() { throw new RuntimeException(); }

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) {}
}