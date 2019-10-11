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
public interface NodeHook {
    void accept(@NotNull final FlowMap o) throws NodeException;
}
