package scraper.addons;

import scraper.annotations.NotNull;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;

import static scraper.api.node.container.NodeLogLevel.*;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class SimpleLogNode implements Node {

    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {
        n.log(TRACE, "1");
        n.log(DEBUG, "2");
        n.log(INFO, "3");
        n.log(WARN, "4");
        n.log(ERROR, "5");
        return n.forward(o);
    }
}