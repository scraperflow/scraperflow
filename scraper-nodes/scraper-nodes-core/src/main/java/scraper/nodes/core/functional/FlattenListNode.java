package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.List;
import java.util.stream.Collectors;

/**
 */
@NodePlugin("0.3.0")
public final class FlattenListNode implements FunctionalNode {

    /** The list with list elements to flatten */
    @FlowKey(defaultValue = "\"{list}\"") @NotNull
    private final T<List<List<?>>> flatten = new T<>(){};

    /** Where the output hash is stored */
    @FlowKey(defaultValue = "\"output\"") @NotNull
    private L<List<?>> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        List<List<?>> flatten = o.eval(this.flatten);
        List<?> flattened = flatten.stream().flatMap(List::stream).distinct().collect(Collectors.toList());

        o.output(output, flattened);
    }
}
