package scraper.nodes.core.functional;

import scraper.annotations.*;
import scraper.api.*;

/**
 * Can modify the current flow map.
 * Can be used to construct a subset of user defined JSON objects.
 */
@NodePlugin("1.0.0")
public class Let<A> implements FunctionalNode {

    /** Element to output */
    @FlowKey(mandatory = true)
    private final T<A> value = new T<>(){};

    /** Location of output */
    @FlowKey(mandatory = true)
    private final L<A> put = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        o.output(put, o.eval(value));
    }
}
