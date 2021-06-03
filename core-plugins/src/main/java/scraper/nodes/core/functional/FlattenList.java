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
@NodePlugin("0.5.0")
public final class FlattenList <A> implements FunctionalNode {

    /** The list with list elements to flatten */
    @FlowKey(mandatory = true)
    private final T<List<List<A>>> flatten = new T<>(){};

    /** Where the flattened list is stored */
    @FlowKey(mandatory = true)
    private final L<List<A>> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        List<List<A>> flatten = o.eval(this.flatten);
        List<A> flattened = flatten.stream().flatMap(List::stream).distinct().collect(Collectors.toList());
        o.output(output, flattened);
    }
}
