package scraper.nodes.core.flow;

import scraper.annotations.*;
import scraper.api.*;

/**
 * Provides if-then-else routing.
 */
@NodePlugin(value = "0.1.0")
public final class IfThenElse implements Node {

    /** Boolean condition */
    @FlowKey(mandatory = true)
    private final T<Boolean> condition = new T<>(){};

    /** True target address */
    @FlowKey
    @Flow(label = "true")
    private Address trueTarget;

    /** False target address */
    @FlowKey
    @Flow(label = "false")
    private Address falseTarget;

    @NotNull
    @Override
    public void process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        Boolean condition = o.eval(this.condition);

        if(condition) {
            if(trueTarget != null)
                n.forward(o, trueTarget);
        } else {
            if(falseTarget != null)
                n.forward(o, falseTarget);
        }
    }
}
