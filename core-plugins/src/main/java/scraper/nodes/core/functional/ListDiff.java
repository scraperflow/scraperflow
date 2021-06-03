package scraper.nodes.core.functional;

import scraper.annotations.*;
import scraper.api.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Takes a universe and removes all elements in a given list from it.
 */
@NodePlugin("0.3.0")
public final class ListDiff <K> implements FunctionalNode {

    /** The universe from which to remove all elements of another list */
    @FlowKey(mandatory = true)
    private final T<List<K>> universe = new T<>(){};

    /** Elements to remove */
    @FlowKey(mandatory = true)
    private final T<List<K>> list = new T<>(){};

    /** Where the difference list is stored */
    @FlowKey(mandatory = true)
    private final L<List<K>> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        Set<K> universe = new HashSet<>(o.eval(this.universe)) ;
        Set<K> list = new HashSet<>(o.eval(this.list));

        universe.removeAll(list);

        o.output(output, new LinkedList<>(universe));
    }
}
