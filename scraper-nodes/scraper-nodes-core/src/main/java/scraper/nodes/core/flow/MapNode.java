package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractNode;
import scraper.core.Template;
import scraper.util.NodeUtil;

import java.util.List;

import static scraper.util.NodeUtil.flowOf;

/**
 * Expects a list at goTo key.
 * Forks new flows for every element in the goTo list.
 * Does not wait or join the forked flows.
 * The element is put on a specified key.
 *
 * @author Albert Schimpf
 */
@NodePlugin("1.0.0")
public final class MapNode extends AbstractNode {

    /** The expected list is located to fork on */
    @FlowKey(mandatory = true)
    private Template<List<?>> list = new Template<>(){};

    /** At which key to put the element of the list into. */
    @FlowKey(defaultValue = "\"element\"")
    private String putElement;

    @FlowKey(mandatory = true)
    private String mapTarget;

    @NotNull
    @Override
    public FlowMap process(@NotNull final FlowMap o) throws NodeException {
        List<?> targetList = list.eval(o);

        targetList.forEach(t -> {
            FlowMap finalCopy = flowOf(o);
            finalCopy.put(putElement, t);
            forkDispatch(finalCopy, NodeUtil.addressOf(mapTarget));
        });

        return forward(o);
    }
}
