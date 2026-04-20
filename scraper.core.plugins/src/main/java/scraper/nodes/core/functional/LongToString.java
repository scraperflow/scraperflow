package scraper.nodes.core.functional;

import scraper.annotations.*;
import scraper.api.*;

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
