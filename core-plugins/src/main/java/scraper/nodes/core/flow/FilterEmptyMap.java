package scraper.nodes.core.flow;

import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.T;

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
