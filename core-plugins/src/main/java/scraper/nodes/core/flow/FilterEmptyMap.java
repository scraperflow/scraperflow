package scraper.nodes.core.flow;

import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.NodeException;
import scraper.api.FlowMap;
import scraper.api.NodeContainer;
import scraper.api.Node;
import scraper.api.T;

/**
 * Stops forwarding if a given map is empty.
 */
@NodePlugin("0.1.0")
public final class FilterEmptyMap<K, V> implements Node {

    /** Map to filter. */
    @FlowKey(mandatory = true)
    private final T<java.util.Map<K, V>> map = new T<>(){};

    @Override
    public void process(NodeContainer<? extends Node> n, FlowMap o) throws NodeException {
        java.util.Map<K, V> map = o.eval(this.map);
        if(!map.isEmpty()) n.forward(o);
    }

}
