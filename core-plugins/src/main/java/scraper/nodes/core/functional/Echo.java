package scraper.nodes.core.functional;

import scraper.annotations.*;
import scraper.api.*;

/**
 * Can modify the current flow map.
 * Can be used to construct a subset of user defined JSON objects.
 * @deprecated use Let instead
 */
@NodePlugin(value = "2.0.2", deprecated = true)
public class Echo <A> implements FunctionalNode {

    /** Element to output */
    @FlowKey
    private final T<A> value = new T<>(){};

    /** Location of output */
    @FlowKey(defaultValue = "\"_\"")
    private final L<A> put = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        o.evalMaybe(value).ifPresent(v -> o.output(put, o.eval(value)));
    }
}
