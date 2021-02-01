package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.Flow;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.L;
import scraper.api.template.T;
import scraper.util.TemplateUtil;

import java.util.List;

/**
 * Applies a fold to a list with an accumulator
 * Example:
 * <pre>
 *
 * type: Fold
 * foldTarget: fold-list
 *
 * putAccumulate: acc
 * putElement: e
 * accumulatorValue: 0
 * list: ["1","2"]
 *
 * result: acc
 * </pre>
 */
@NodePlugin(value = "0.2.0", customFlowAfter = true)
public final class Fold <A,B> implements Node {

    /** Label of fold target */
    @FlowKey(mandatory = true)
    @Flow(dependent = true, crossed = true, label = "fold")
    private Address foldTarget;

    /** List to apply fold */
    @FlowKey(mandatory = true)
    private final T<List<A>> list = new T<>(){};

    /** At which key to put the element of the accumulator into. */
    @FlowKey(defaultValue = "\"_\"")
    private final L<B> putAccumulate = new L<>(){};

    /** At which key to retrieve the intermediate result of the accumulator into. */
    @FlowKey(defaultValue = "\"_\"")
    private final L<B> retrieveAccumulate = new L<>(){};

    /** At which key to put the element of the list into. */
    @FlowKey(defaultValue = "\"_\"")
    private final L<A> putElement = new L<>(){};

    /** At which key to put result */
    @FlowKey(defaultValue = "\"_\"")
    private final L<B> result = new L<>(){};

    /** Default accumulator value */
    @FlowKey(defaultValue = "\"none\"")
    private final T<B> accumulatorValue = new T<>(){};


    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {
        List<A> list = o.eval(this.list);

        B accumulatorValue = o.eval(this.accumulatorValue);
        for (A e : list) {
            o.output(putElement, e);
            o.output(putAccumulate, accumulatorValue);
            o = n.eval(o, foldTarget);

            // retrieve new acc value
            @SuppressWarnings("unchecked") // statically checked
            T<B> acc = (T<B>) TemplateUtil.templateOf(retrieveAccumulate);
            accumulatorValue = o.eval(acc);
        }


        // finish
        o.output(result, accumulatorValue);

        // continue
        return o;
    }
}
