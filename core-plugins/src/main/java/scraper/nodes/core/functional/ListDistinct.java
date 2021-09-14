package scraper.nodes.core.functional;

import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.annotations.NotNull;
import scraper.api.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Filters only distinct elements in a list.
 */
@NodePlugin("0.1.0")
public final class ListDistinct<K> implements FunctionalNode {

    /** List to distinct */
    @FlowKey(mandatory = true)
    private final T<List<K>> list = new T<>(){};

    /** Distincted list */
    @FlowKey(mandatory = true)
    private final L<List<K>> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        List<K> l = o.eval(this.list);
        o.output(output, l.stream().distinct().collect(Collectors.toList()));
    }
}
