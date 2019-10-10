package scraper.addons;

import scraper.annotations.NotNull;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractNode;
import scraper.core.NodeLogLevel;

import static scraper.core.NodeLogLevel.*;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class SimpleLogNode extends TestNode {
    @NotNull
    @Override
    public FlowMap process(@NotNull final FlowMap o) throws NodeException {
        log(TRACE, "1");
        log(DEBUG, "2");
        log(INFO, "3");
        log(WARN, "4");
        log(ERROR, "5");
        return forward(o);
    }
}