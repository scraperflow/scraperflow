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
 * <p>
 * Example
 * <pre>
 * type: EchoNode
 * put: id
 * value: "{id}"
 * </pre>
 */
@NodePlugin("0.0.1")
public class IntToStringNode implements FunctionalNode {

    /** Element String */
    @FlowKey(mandatory = true)
    private final T<Integer> integer = new T<>(){};

    /** Integer Output */
    @FlowKey(mandatory = true)
    private final L<String> string = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        Integer value = o.eval(this.integer);
        // put object
        o.output(string, String.valueOf(value));
    }
}
