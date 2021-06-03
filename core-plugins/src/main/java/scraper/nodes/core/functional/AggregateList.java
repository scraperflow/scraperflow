package scraper.nodes.core.functional;


import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

import java.util.List;

/**
 * Appends an element to a list containing objects of the same type.
 */
@NodePlugin("0.2.0")
public final class AggregateList <K> implements FunctionalNode {

    /** Evaluated content inserted into <var>put</var> */
    @FlowKey(mandatory = true)
    private final T<K> aggregate = new T<>(){};

    /** The input list */
    @FlowKey(mandatory = true)
    private final T<List<K>> aggregateList = new T<>(){};

    /** Only put distinct elements into list if enabled */
    @FlowKey(defaultValue = "false")
    private final T<Boolean> distinct = new T<>(){};

    /** Expects a List object at the <var>put</var> key or generates a new empty list if there is no list present */
    @FlowKey(mandatory = true)
    private final L<List<K>> result = new L<>(){};


    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        List<K> resultList = o.eval(aggregateList);
        K eval = o.eval(aggregate);

        if(!o.eval(distinct)) resultList.add(eval);
        else if (!resultList.contains(eval)) resultList.add(eval);

        o.output(result, resultList);
    }
}
