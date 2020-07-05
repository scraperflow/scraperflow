package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

/**
 * @author Albert Schimpf
 */
@NodePlugin(value = "0.0.1")
public final class ReadFileDummyNode implements FunctionalNode {

    @FlowKey
    private T<String> inputFile = new T<>(){};

    @FlowKey
    private L<String> output = new L<>(){};

    @FlowKey
    private String charset;

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) throws NodeException {
        // do nothing as pure as it gets
    }
}
