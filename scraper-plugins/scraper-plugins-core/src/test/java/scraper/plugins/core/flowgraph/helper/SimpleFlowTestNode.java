package scraper.plugins.core.flowgraph.helper;

import scraper.annotations.NotNull;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;

/**
 * @author Albert Schimpf
 */
@NodePlugin(value = "0.0.1")
public final class SimpleFlowTestNode extends AbstractFunctionalNode {
    @Override
    public void modify(@NotNull final FlowMap o) {}
}
