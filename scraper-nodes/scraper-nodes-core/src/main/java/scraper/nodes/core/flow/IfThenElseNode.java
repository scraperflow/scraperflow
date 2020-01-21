package scraper.nodes.core.flow;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.core.AbstractNode;
import scraper.core.Template;

/**
 * Provides if-then-else routing
 *
 * @see AbstractNode
 * @since 0.1
 * @author Albert Schimpf
 */
@NodePlugin("0.1.0")
public final class IfThenElseNode extends AbstractNode {

    @FlowKey(mandatory = true) @NotNull
    private Template<Boolean> condition = new Template<>(){};

    @FlowKey @Nullable
    private Address trueTarget;

    @FlowKey @Nullable
    private Address falseTarget;

    @NotNull
    @Override
    public FlowMap process(@NotNull final FlowMap o) throws NodeException {
        Boolean condition = this.condition.eval(o);

        if(condition) {
            if(trueTarget != null)
                return forward(eval(o, trueTarget));
        } else {
            if(falseTarget != null)
                return forward(eval(o, falseTarget));
        }

        return forward(o);
    }
}
