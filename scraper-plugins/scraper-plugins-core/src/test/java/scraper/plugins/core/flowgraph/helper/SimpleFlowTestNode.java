package scraper.plugins.core.flowgraph.helper;

import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;

/**
 * @author Albert Schimpf
 */
@NodePlugin(value = "0.0.1")
public final class SimpleFlowTestNode implements FunctionalNode {

    @Override
    public void modify(FunctionalNodeContainer n, FlowMap o) throws NodeException {
        // do nothing as pure as it gets
    }
}
