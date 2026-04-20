package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.NodeException;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

/**
 * @author Albert Schimpf
 */
@NodePlugin(value = "0.0.1", deprecated = true)
public final class ReadFileDummy implements FunctionalNode {

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
