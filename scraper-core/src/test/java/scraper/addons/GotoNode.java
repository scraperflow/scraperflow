package scraper.addons;

import scraper.annotations.NotNull;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractNode;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class GotoNode extends TestNode {

    @NotNull
    @Override
    public FlowMap process(@NotNull FlowMap o) throws NodeException {
        return forward(o);
    }
}