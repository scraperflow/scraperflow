package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.List;

/**
 * Flattens a list of strings into a flattened string.
 */
@NodePlugin("0.0.1")
public final class FlattenStringList implements FunctionalNode {

    /** The list of strings   */
    @FlowKey(mandatory = true)
    private final T<List<String>> list = new T<>(){};

    /** Delimiter. Defaults to space. */
    @FlowKey(defaultValue = "\" \"")
    private String delimiter;

    /** Where the flattened string is stored */
    @FlowKey(defaultValue = "\"_\"")
    private final L<String> string = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        List<String> flatten = o.eval(this.list);
        o.output(string, String.join(delimiter, flatten));
    }
}
