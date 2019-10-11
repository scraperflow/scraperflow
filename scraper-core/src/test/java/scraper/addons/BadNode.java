package scraper.addons;

import scraper.annotations.NotNull;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;

@NodePlugin(value = "0.1.0")
public final class BadNode extends AbstractFunctionalNode {
    public BadNode () { throw new RuntimeException(); }

    @Override
    public void modify(@NotNull final FlowMap o) {}
}