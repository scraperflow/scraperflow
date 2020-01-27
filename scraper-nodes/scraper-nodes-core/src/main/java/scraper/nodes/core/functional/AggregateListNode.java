package scraper.nodes.core.functional;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;
import scraper.core.Template;

import java.util.LinkedList;
import java.util.List;

/**
 * Appends a template to a list.
 */
@NodePlugin("1.0.0")
public final class AggregateListNode extends AbstractFunctionalNode {

    /** Evaluated content inserted into 'put' */
    @FlowKey(mandatory = true)
    private final Template<Object> aggregate = new Template<>(){};

    /** Expects a List object at the 'put' key or generates a new empty list if there is no list at given key */
    @FlowKey(mandatory = true, output = true)
    private final Template<List<Object>> put = new Template<>(){};

    /** Only put distinct elements into list if enabled */
    @FlowKey(defaultValue = "false")
    private Boolean distinct;

    @Override
    public void modify(@NotNull final FlowMap o) {
        // fetch the list, or create a new one if it does not exist
        List<Object> resultList = put.evalOrDefault(o, new LinkedList<>());

        // evaluate template
        Object eval = aggregate.eval(o);

        // append
        if(!distinct) resultList.add(eval);
        else if (!resultList.contains(eval)) resultList.add(eval);

        put.output(o, resultList);
    }
}
