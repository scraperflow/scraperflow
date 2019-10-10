package scraper.api.node;

import scraper.annotations.NotNull;
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
    default @NotNull FlowMap accept(@NotNull final FlowMap o) throws NodeException {
        for (NodeHook hook : beforeHooks()) { hook.accept(o); }
        FlowMap fm = process(o);
        for (NodeHook hook : afterHooks()) { hook.accept(o); }
        return fm;
    }

    default @NotNull Collection<NodeHook> afterHooks() { return Set.of(); }
    default @NotNull Collection<NodeHook> beforeHooks() {return Set.of(); }

    @NotNull FlowMap process(@NotNull FlowMap o) throws NodeException;

}
