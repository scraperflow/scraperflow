package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

import java.util.List;

import static scraper.nodes.core.functional.BooleanOp.BooleanOperator.AND;

/**
 * Boolean operation on lists.
 */
@NodePlugin("0.0.2")
public class BooleanOp implements FunctionalNode {

    /** List of boolean */
    @FlowKey(mandatory = true)
    private final T<List<Boolean>> list = new T<>(){};

    /** Initial value */
    @FlowKey(defaultValue = "true")
    private final T<Boolean> defaultValue = new T<>(){};

    /** Conjunction result */
    @FlowKey(mandatory = true)
    private final L<Boolean> result = new L<>(){};


    /** Operator for comparison
     * <ul>
     *     <li>AND
     *     <li>OR
     * </ul>
     */
    @FlowKey(mandatory = true)
    private final T<BooleanOp.BooleanOperator> op = new T<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        var bools = o.eval(list);
        var op = o.eval(this.op);

        boolean end = o.eval(defaultValue);

        for (Boolean bool : bools) {
            if (op == AND) end = end && bool;
            else           end = end || bool;
        }

        o.output(result, end);
    }

    enum BooleanOperator {
        AND, OR
    }
}
