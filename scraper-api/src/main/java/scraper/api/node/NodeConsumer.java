package scraper.api.node;

import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;

import java.util.Collection;
import java.util.Set;

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
    default FlowMap accept(final FlowMap o) throws NodeException {
        for (NodeHook hook : beforeHooks()) { hook.accept(o); }
        FlowMap fm = process(o);
        for (NodeHook hook : afterHooks()) { hook.accept(o); }
        return fm;
    }

    default Collection<NodeHook> afterHooks() { return Set.of(); }
    default Collection<NodeHook> beforeHooks() {return Set.of(); }

    FlowMap process(FlowMap o) throws NodeException;

}
