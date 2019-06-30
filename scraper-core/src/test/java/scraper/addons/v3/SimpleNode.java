package scraper.addons.v3;

import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractNode;

@NodePlugin(value = "1.0.0", deprecated = true)
public final class SimpleNode extends AbstractNode {
    @Override
    public FlowMap process(final FlowMap o) {return o;}
}