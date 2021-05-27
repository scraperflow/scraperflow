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
