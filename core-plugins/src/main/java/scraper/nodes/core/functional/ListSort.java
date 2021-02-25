package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.lang.Math.min;

/**
 * Sorts a list.
 */
@NodePlugin("0.1.0")
public final class ListSort<K> implements FunctionalNode {

    /** List to sort */
    @FlowKey(defaultValue = "[]")
    private final T<List<K>> list = new T<>(){};

    /** Sorted list */
    @FlowKey(mandatory = true)
    private final L<List<K>> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        List<K> l = o.eval(this.list);
        o.output(output, l.stream().sorted().collect(Collectors.toList()));
    }
}
