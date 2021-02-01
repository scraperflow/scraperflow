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
 * Flattens a list of lists into a flattened list.
 * <p>
 * Can be used to merge lists by using templates:
 * <pre>
 *type: FlattenListNode
 *flatten: ["{list1}", "{list2}"]
 * </pre>
 */
@NodePlugin("0.4.1")
public final class FlattenList <A> implements FunctionalNode {

    /** The list with list elements to flatten */
    @FlowKey(defaultValue = "\"{list}\"")
    private final T<List<List<A>>> flatten = new T<>(){};

    /** Where the flattened list is stored */
    @FlowKey(defaultValue = "\"_\"")
    private final L<List<A>> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        List<List<A>> flatten = o.eval(this.flatten);
        List<A> flattened = flatten.stream().flatMap(List::stream).distinct().collect(Collectors.toList());

        o.output(output, flattened);
    }
}
