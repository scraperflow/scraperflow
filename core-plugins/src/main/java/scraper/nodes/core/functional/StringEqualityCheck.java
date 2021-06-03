package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

/**
 * Checks string equality for two inputs.
 */
@NodePlugin("0.1.0")
public final class StringEqualityCheck implements FunctionalNode {

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
