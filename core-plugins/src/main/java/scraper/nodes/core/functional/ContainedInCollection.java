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
 * Sets a flag depending if the object is contained in the list
 */
@NodePlugin("0.4.0")
public final class ContainedInCollection <A> implements FunctionalNode {

    /** Collection to be checked if the object is contained */
    @FlowKey(mandatory = true)
    private final T<List<A>> collection = new T<>(){};

    /** This evaluated object is used for checking */
    @FlowKey(mandatory = true)
    private final T<A> object = new T<>(){};

    /** Determines if the <code>contains</code> or <code>not contains</code> operation is used */
    @FlowKey(defaultValue = "false")
    private final T<Boolean> negate = new T<>(){};

    /** Key where the result flag will be written to */
    @FlowKey(mandatory = true)
    private final L<Boolean> flag = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        A       object     = o.eval(this.object);
        List<A> collection = o.eval(this.collection);

        // cont negate output
        // 0    0      0
        // 0    1      1
        // 1    0      1
        // 1    1      0
        Boolean result = collection.contains(object) ^ o.eval(negate);

        o.output(flag, result);
    }
}
