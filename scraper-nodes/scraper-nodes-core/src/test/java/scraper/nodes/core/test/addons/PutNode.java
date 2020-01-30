package scraper.nodes.core.test.addons;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;

@NodePlugin(value = "1.2.3")
public final class PutNode implements FunctionalNode {
    @FlowKey private String toPut;

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) throws NodeException {
        o.put("hello", "world"); o.put(toPut, "result");
    }
}