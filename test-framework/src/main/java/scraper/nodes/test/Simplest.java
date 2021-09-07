package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.NodePlugin;
import scraper.api.NodeException;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;

import static scraper.api.NodeLogLevel.INFO;

@NodePlugin(value = "1.2.3")
public final class Simplest implements FunctionalNode {

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) throws NodeException {
        n.log(INFO, "pure");
    }
}