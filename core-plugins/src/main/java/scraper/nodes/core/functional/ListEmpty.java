package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

import java.util.List;

/**
 * Checks if a list is empty.
 */
@NodePlugin("0.1.0")
public final class ListEmpty <K> implements FunctionalNode {

    /** List to check the size */
    @FlowKey(mandatory = true)
    private final T<List<K>> list = new T<>(){};

    /** Where the boolean result is stored */
    @FlowKey(mandatory = true)
    private final L<Boolean> put = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        o.output(put, o.eval(list).isEmpty());
    }
}
