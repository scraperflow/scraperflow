package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

/**
 * Can modify the current flow map.
 * Can be used to construct a subset of user defined JSON objects.
 */
@NodePlugin("2.0.2")
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
