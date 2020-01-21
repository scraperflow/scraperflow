package scraper.nodes.core.functional;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;
import scraper.core.Template;

import java.util.Collection;

/**
 * Sets a flag depending if the goTo object is contained in the collection.
 *
 * @author Albert Schimpf
 */
@NodePlugin("1.0.0")
public final class ContainedInCollectionNode extends AbstractFunctionalNode {

    /** Collection to be checked if the object is contained */
    @FlowKey(defaultValue = "\"{collection}\"") @NotNull
    private final Template<Collection> collection = new Template<>(){};

    /** This evaluated object is used for checking */
    @FlowKey(defaultValue = "\"{object}\"") @NotNull
    private final Template<Object> object = new Template<>(){};

    /** Key where the result flag will be written to */
    @FlowKey(defaultValue = "\"flag\"", output = true) @NotNull
    private Template<Boolean> flag = new Template<>(){};

    /** Determines if the contains or contains not operation is used */
    @FlowKey(defaultValue = "false")
    private Boolean negate;

    @Override
    public void modify(@NotNull final FlowMap o) {
        Object     object     = this.object.input(o);
        Collection collection = this.collection.input(o);

        // cont negate output
        // 0    0      0
        // 0    1      1
        // 1    0      1
        // 1    1      0
        Boolean result = collection.contains(object) ^ negate;

        flag.output(o, result);
    }
}
