package scraper.nodes.core.functional;

import scraper.annotations.*;
import scraper.api.*;

import java.util.List;

/**
 * Sum integer list.
 */
@NodePlugin("0.1.0")
public class Sum implements FunctionalNode {

    /** Integers */
    @FlowKey(mandatory = true)
    private final T<List<Integer>> integers = new T<>(){};

    /** Integer Output */
    @FlowKey(mandatory = true)
    private final L<Integer> result = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        o.output(result, o.eval(integers).stream().reduce(0, Integer::sum));
    }
}
