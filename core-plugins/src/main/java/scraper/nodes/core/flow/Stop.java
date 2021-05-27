package scraper.nodes.core.flow;

import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.T;

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
