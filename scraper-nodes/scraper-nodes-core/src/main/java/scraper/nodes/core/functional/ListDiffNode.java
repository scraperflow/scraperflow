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
@NodePlugin("0.1.0")
public final class ListDiffNode implements FunctionalNode {

    /** The list with list elements to flatten */
    @FlowKey(defaultValue = "\"{universe}\"") @NotNull
    private final T<List<?>> universe = new T<>(){};

    @FlowKey
    private final T<List<?>> list = new T<>(){};

    /** Where the output hash is stored */
    @FlowKey(defaultValue = "\"output\"") @NotNull
    private L<List<?>> output = new L<>(){};

    @SuppressWarnings("SuspiciousMethodCalls") // removeAll that's ok
    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        Set<?> universe = new HashSet<>(o.eval(this.universe)) ;
        Set<?> list = new HashSet<>(o.eval(this.list));

        universe.removeAll(list);

        o.output(output, new LinkedList<>(universe));
    }
}
