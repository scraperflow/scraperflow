package scraper.nodes.core.functional;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.LinkedList;
import java.util.List;

/**
 * Appends an element to a list containing objects of the same type.
 */
@NodePlugin("0.2.0")
public final class AggregateListNode<K> implements FunctionalNode {

    /** Evaluated content inserted into 'put' */
    @FlowKey(mandatory = true)
    private final T<K> aggregate = new T<>(){};

    /** Expects a List object at the 'put' key or generates a new empty list if there is no list at given key */
    @FlowKey(mandatory = true)
    private final L<List<K>> put = new L<>(){};

    /** Only put distinct elements into list if enabled */
    @FlowKey(defaultValue = "false")
    private Boolean distinct;

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        // fetch the list, or create a new one if it does not exist
        List<K> resultList = o.evalOrDefault(put, new LinkedList<>());

        // evaluate T
        K eval = o.eval(aggregate);

        // append
        if(!distinct) resultList.add(eval);
        else if (!resultList.contains(eval)) resultList.add(eval);

        o.output(put, resultList);
    }
}
