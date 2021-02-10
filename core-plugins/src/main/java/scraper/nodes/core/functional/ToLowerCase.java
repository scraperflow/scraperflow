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
 * String to lower case
 */
@NodePlugin("0.0.1")
public class ToLowerCase implements FunctionalNode {

    /** String input */
    @FlowKey(mandatory = true)
    private final T<String> string = new T<>(){};

    /** Integer output */
    @FlowKey(mandatory = true)
    private final L<String> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        String value = o.eval(this.string);
        o.output(output, value.toLowerCase());
    }
}
