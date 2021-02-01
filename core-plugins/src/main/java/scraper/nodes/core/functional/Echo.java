package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.Optional;

/**
 * Can modify the current flow map.
 * Can be used to construct user defined JSON objects.
 * <p>
 * Example
 * <pre>
 * type: EchoNode
 * put: id
 * value: "{id}"
 * </pre>
 */
@NodePlugin("2.0.1")
public class Echo <A> implements FunctionalNode {

    /** Element to output */
    @FlowKey
    private final T<A> value = new T<>(){};

    /** Location of output */
    @FlowKey(defaultValue = "\"_\"")
    private final L<A> put = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        Optional<A> value = o.evalMaybe(this.value);
        // put object
        value.ifPresent(v -> o.output(put, v));
    }
}
