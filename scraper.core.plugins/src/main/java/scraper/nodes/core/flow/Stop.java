package scraper.nodes.core.flow;

import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.NodeException;
import scraper.api.FlowMap;
import scraper.api.NodeContainer;
import scraper.api.Node;
import scraper.api.T;

/**
 * Stops forwarding if condition is true
 */
@NodePlugin("0.1.0")
public final class Stop implements Node {

    /** Boolean to filter. */
    @FlowKey(mandatory = true)
    private final T<Boolean> stop = new T<>(){};

    @Override
    public void process(NodeContainer<? extends Node> n, FlowMap o) throws NodeException {
        if(!o.eval(stop)) n.forward(o);
    }

}
