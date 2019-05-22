package scraper.addons;

import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class BadNode extends AbstractFunctionalNode {
    public BadNode () { throw new RuntimeException(); }

    @Override
    public void modify(final FlowMap o) {}
}