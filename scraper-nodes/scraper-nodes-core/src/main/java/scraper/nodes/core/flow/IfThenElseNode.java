package scraper.nodes.core.flow;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.T;

/**
 * Provides if-then-else routing
 */
@NodePlugin("0.1.0")
public final class IfThenElseNode implements Node {

    /** Boolean condition*/
    @FlowKey(mandatory = true)
    private T<Boolean> condition = new T<>(){};

    /** True target address */
    @FlowKey
    private Address trueTarget;

    /** False target address */
    @FlowKey
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
