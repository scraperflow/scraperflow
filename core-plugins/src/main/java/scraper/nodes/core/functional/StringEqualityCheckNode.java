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
 * Checks string equality for two inputs.
 */
@NodePlugin("0.1.0")
public final class StringEqualityCheckNode implements FunctionalNode {

    /** String 1 */
    @FlowKey(mandatory = true)
    private final T<String> string = new T<>(){};

    /** String 2 */
    @FlowKey(mandatory = true)
    private final T<String> check = new T<>(){};

    /** Result location */
    @FlowKey(defaultValue = "\"output\"")
    private final L<Boolean> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        String content = o.eval(this.string);
        String check = o.eval(this.check);
        o.output(output, content.equals(check));
    }
}
