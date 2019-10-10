package scraper.addons.v3;

import scraper.addons.TestNode;
import scraper.annotations.NotNull;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractNode;

@NodePlugin(value = "1.0.0", deprecated = true)
public final class SimpleNode extends TestNode {
    @NotNull
    @Override
    public FlowMap process(@NotNull final FlowMap o) {return o;}
}