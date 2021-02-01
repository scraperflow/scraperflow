package scraper.nodes.core.flow;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.annotations.node.Flow;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.T;

/**
 * Provides if-then-else routing
 */
@NodePlugin(value = "0.1.0", customFlowAfter = true)
public final class IfThenElse implements Node {

    /** Boolean condition*/
    @FlowKey(mandatory = true)
    private final T<Boolean> condition = new T<>(){};

    /** True target address */
    @FlowKey
    @Flow(dependent = true, crossed = false, label = "true")
    private Address trueTarget;

    /** False target address */
    @FlowKey
    @Flow(dependent = true, crossed = false, label = "false")
    private Address falseTarget;

    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {
        Boolean condition = o.eval(this.condition);

        if(condition) {
            if(trueTarget != null)
                return n.eval(o, trueTarget);
        } else {
            if(falseTarget != null)
                return n.eval(o, falseTarget);
        }

        return o;
    }
}
