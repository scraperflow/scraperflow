package scraper.nodes.core.flow;

import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.NodeException;
import scraper.api.FlowMap;
import scraper.api.NodeContainer;
import scraper.api.Node;
import scraper.api.T;

import java.util.List;

/**
 * Stops forwarding if a given list is empty.
 */
@NodePlugin("0.1.0")
public final class FilterEmptyList<A> implements Node {

    /** List to filter. */
    @FlowKey(mandatory = true)
    private final T<List<A>> list = new T<>(){};

    @Override
    public void process(NodeContainer<? extends Node> n, FlowMap o) throws NodeException {
        List<A> list = o.eval(this.list);
        if(!list.isEmpty()) n.forward(o);
    }

}
