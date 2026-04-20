package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.NodePlugin;
import scraper.api.NodeException;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;

/**
 * @author Albert Schimpf
 */
@NodePlugin(value = "0.0.1", deprecated = true)
public final class SimpleFlowTest implements FunctionalNode {

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) throws NodeException {
        // do nothing as pure as it gets
    }
}
