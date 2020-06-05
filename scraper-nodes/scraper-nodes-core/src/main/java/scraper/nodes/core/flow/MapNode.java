package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.List;


/**
 * Forks new flows for every element in <var>list</var>.
 * Does not wait or join the forked flows.
 * The element is saved to <var>putElement</var>
 */
@NodePlugin("0.4.0")
public final class MapNode<K> implements Node {

    /** The expected list is located to fork on */
    @FlowKey(mandatory = true)
    private T<List<K>> list = new T<>(){};

    /** Target address to fork to */
    @FlowKey(mandatory = true)
    private Address mapTarget;

    /** At which key to put the element of the list into. */
    @FlowKey(defaultValue = "\"element\"")
    private final L<K> putElement = new L<>(){};

    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        List<K> targetList = o.eval(list);

        targetList.forEach(t -> {
            FlowMap finalCopy = o.copy();
            finalCopy.output(putElement, t);
            n.forkDispatch(finalCopy, mapTarget);
        });

        return o;
    }
}
