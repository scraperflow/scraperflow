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
 * Sum two integers
 */
@NodePlugin("0.0.1")
public class Sum implements FunctionalNode {

    /** Operand 1 */
    @FlowKey(mandatory = true)
    private final T<Integer> integer = new T<>(){};

    /** Operand 2 */
    @FlowKey(mandatory = true)
    private final T<Integer> integer2 = new T<>(){};

    /** Integer Output */
    @FlowKey(mandatory = true)
    private final L<Integer> result = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        o.output(result, o.eval(integer) + o.eval(integer2));
    }
}
