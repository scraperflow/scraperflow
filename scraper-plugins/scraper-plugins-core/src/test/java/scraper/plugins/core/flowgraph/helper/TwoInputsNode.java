package scraper.plugins.core.flowgraph.helper;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.T;

/**
 * @author Albert Schimpf
 */
@NodePlugin(value = "0.0.1")
public final class TwoInputsNode implements FunctionalNode {

    @FlowKey
    private T<String> input1 = new T<>(){};

    @FlowKey
    private T<String> input2 = new T<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) throws NodeException {
        // do nothing as pure as it gets
    }
}
