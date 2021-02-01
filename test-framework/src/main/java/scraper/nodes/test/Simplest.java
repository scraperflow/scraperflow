package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;

import static scraper.api.node.container.NodeLogLevel.INFO;

@NodePlugin(value = "1.2.3")
public final class Simplest implements FunctionalNode {

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) throws NodeException {
        n.log(INFO, "pure");
    }
}