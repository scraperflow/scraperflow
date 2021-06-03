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
 * Convert an long to a String.
 */
@NodePlugin("0.0.1")
public class LongToString implements FunctionalNode {

    /** Element String */
    @FlowKey(mandatory = true)
    private final T<Long> Long = new T<>(){};

    /** Integer Output */
    @FlowKey(mandatory = true)
    private final L<String> string = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        Long value = o.eval(this.Long);
        o.output(string, String.valueOf(value));
    }
}
