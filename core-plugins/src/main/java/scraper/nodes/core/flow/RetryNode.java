package scraper.nodes.core.flow;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;

import static scraper.api.node.container.NodeLogLevel.ERROR;
import static scraper.api.node.container.NodeLogLevel.INFO;


/**
 * Provides a try-catch mechanism
 */
@NodePlugin("0.3.0")
public final class RetryNode implements Node {

    /** How many times the flow is retried */
    @FlowKey(defaultValue = "5")
    private Integer retry;

    /** Target of the retry */
    @FlowKey(mandatory = true)
    private Address retryTarget;

    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {
        int current = 0;
        while(current < retry) {
            FlowMap localCopy = o.copy();
            try {
                return n.eval(localCopy, retryTarget);
            } catch (NodeException e) {
                n.log(INFO, "Retry {}", (current+1));
                current++;
            }
        }

        n.log(ERROR, "Retry count exceeded");
        throw new NodeException("Retry count exceeded");
    }
}
