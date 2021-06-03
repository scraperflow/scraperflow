package scraper.nodes.core.functional;

import scraper.annotations.*;
import scraper.api.*;

import java.util.Collections;
import java.util.List;

/**
 * Reverses a list.
 */
@NodePlugin("0.1.0")
public final class ListReverse<K> implements FunctionalNode {

    /** List to slice */
    @FlowKey(mandatory = true)
    private final T<List<K>> list = new T<>(){};

    /** Reversed list */
    @FlowKey(mandatory = true)
    private final L<List<K>> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        List<K> list = o.eval(this.list);
        Collections.reverse(list);
        o.output(output, list);
    }
}
