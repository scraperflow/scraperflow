package scraper.api.node;

import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;

/**
 * Objects which implement this interface can consume and modify {@link FlowMap}s.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface NodeConsumer {
    /**
     * Accept a {@link FlowMap}.
     * The FlowMap can be modified in the process.
     * Side-effects are possible.
     *
     * @param o The FlowMap to modifiy/use in the function
     * @throws NodeException if there is a processing error during the function call
     */
    void accept(final FlowMap o) throws NodeException;
}
