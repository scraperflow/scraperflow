package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.reflect.T;

import java.util.List;



/**
 * Expects a list at goTo key.
 * Forks new flows for every element in the goTo list.
 * Does not wait or join the forked flows.
 * The element is put on a specified key.
 *
 * @author Albert Schimpf
 */
@NodePlugin("0.2.0")
public final class MapNode implements Node {

    /** The expected list is located to fork on */
    @FlowKey(mandatory = true)
    private T<List<?>> list = new T<>(){};

    /** At which key to put the element of the list into. */
    @FlowKey(defaultValue = "\"element\"")
    private String putElement;

    @FlowKey(mandatory = true)
    private Address mapTarget;

    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        List<?> targetList = o.eval(list);

        targetList.forEach(t -> {
            FlowMap finalCopy = o.copy();
            finalCopy.output(putElement, t);
            n.forkDispatch(finalCopy, mapTarget);
        });

        return o;
    }
}
