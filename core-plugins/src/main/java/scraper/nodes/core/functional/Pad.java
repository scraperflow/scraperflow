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
 * Prefixes strings until a length is reached
 */
@NodePlugin("0.0.1")
public class Pad implements FunctionalNode {

    /** String to pad */
    @FlowKey(mandatory = true)
    private final T<String> value = new T<>(){};

    /** Length to pad to */
    @FlowKey(mandatory = true)
    private final T<Integer> pad = new T<>(){};

    /** Element to insert */
    @FlowKey(defaultValue = "\"0\"")
    private final T<String> element = new T<>(){};

    /** padded string */
    @FlowKey(mandatory = true)
    private final L<String> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        StringBuilder toPad = new StringBuilder(o.eval(this.value));
        Integer pad = o.eval(this.pad);

        for (int i = toPad.length(); i < pad; i++) {
            toPad.insert(0, o.eval(element));
        }

        o.output(this.output, toPad.toString());
    }
}
