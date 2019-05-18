package scraper.addons;

import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractNode;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class SimpleNode extends AbstractNode {
    @Override
    public void accept(final FlowMap o) {}
}