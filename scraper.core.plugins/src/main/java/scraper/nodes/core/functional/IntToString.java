package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

/**
 * Convert an Integer to a String.
 */
@NodePlugin("0.0.1")
public class IntToString implements FunctionalNode {

    /** Element String */
    @FlowKey(mandatory = true)
    private final T<Integer> integer = new T<>(){};

    /** Integer Output */
    @FlowKey(mandatory = true)
    private final L<String> string = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        Integer value = o.eval(this.integer);
        o.output(string, String.valueOf(value));
    }
}
