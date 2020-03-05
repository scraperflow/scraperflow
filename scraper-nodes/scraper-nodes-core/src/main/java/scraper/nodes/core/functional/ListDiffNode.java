package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 */
@NodePlugin("0.2.0")
public final class ListDiffNode<K> implements FunctionalNode {

    /** The list with list elements to flatten */
    @FlowKey(defaultValue = "\"{universe}\"")
    private final T<List<K>> universe = new T<>(){};

    @FlowKey
    private final T<List<K>> list = new T<>(){};

    /** Where the output hash is stored */
    @FlowKey(defaultValue = "\"output\"")
    private L<List<K>> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        Set<K> universe = new HashSet<>(o.eval(this.universe)) ;
        Set<K> list = new HashSet<>(o.eval(this.list));

        universe.removeAll(list);

        o.output(output, new LinkedList<>(universe));
    }
}
