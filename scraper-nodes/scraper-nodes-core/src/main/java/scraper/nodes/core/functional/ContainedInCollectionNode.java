package scraper.nodes.core.functional;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.reflect.T;

import java.util.Collection;

/**
 * Sets a flag depending if the goTo object is contained in the collection.
 *
 * @author Albert Schimpf
 */
@NodePlugin("0.1.0")
public final class ContainedInCollectionNode implements FunctionalNode {

    /** Collection to be checked if the object is contained */
    @FlowKey(defaultValue = "\"{collection}\"") @NotNull
    private final T<Collection> collection = new T<>(){};

    /** This evaluated object is used for checking */
    @FlowKey(defaultValue = "\"{object}\"") @NotNull
    private final T<Object> object = new T<>(){};

    /** Key where the result flag will be written to */
    @FlowKey(defaultValue = "\"flag\"", output = true) @NotNull
    private T<Boolean> flag = new T<>(){};

    /** Determines if the contains or contains not operation is used */
    @FlowKey(defaultValue = "false")
    private Boolean negate;

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        Object     object     = o.eval(this.object);
        Collection collection = o.eval(this.collection);

        // cont negate output
        // 0    0      0
        // 0    1      1
        // 1    0      1
        // 1    1      0
        Boolean result = collection.contains(object) ^ negate;

        o.output(flag, result);
    }
}
