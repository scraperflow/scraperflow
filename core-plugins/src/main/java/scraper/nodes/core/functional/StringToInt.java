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
 * String To Int
 * </pre>
 */
@NodePlugin("0.0.1")
public class StringToInt implements FunctionalNode {


    /** Integer Output */
    @FlowKey(mandatory = true)
    private final T<String> string = new T<>(){};

    /** Element String */
    @FlowKey(mandatory = true)
    private final L<Integer> integer = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        String value = o.eval(this.string);
        // put object
        o.output(integer, Integer.parseInt(value));
    }
}
