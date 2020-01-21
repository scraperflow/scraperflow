package scraper.nodes.core.flow;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractNode;

import static scraper.core.NodeLogLevel.ERROR;
import static scraper.core.NodeLogLevel.INFO;
import static scraper.util.NodeUtil.flowOf;


/**
 * Provides a try-catch mechanism
 *
 * @see AbstractNode
 * @since 0.1
 * @author Albert Schimpf
 */
@NodePlugin("0.1.0")
public final class RetryNode extends AbstractNode {

    @FlowKey(defaultValue = "5")
    private Integer retry;

    @NotNull
    @Override
    public FlowMap process(@NotNull final FlowMap o) throws NodeException {
        int current = 0;
        while(current < retry) {
            FlowMap localCopy = flowOf(o);
            try {
                return forward(localCopy);
            } catch (NodeException e) {
                log(INFO, "Retry {}", (current+1));
                current++;
            }
        }

        log(ERROR, "Retry count exceeded");
        throw new NodeException("Retry count exceeded");
    }
}
