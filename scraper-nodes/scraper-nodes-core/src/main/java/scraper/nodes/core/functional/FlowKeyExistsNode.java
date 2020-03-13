package scraper.nodes.core.functional;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;

/**
 * Sets a flag depending if a key in the flow map exists or not
*/
@NodePlugin("0.9.0")
public final class FlowKeyExistsNode implements FunctionalNode {

    /** Key to check */
    @FlowKey(mandatory = true)
    private String key;

    /** Where to put the result of the check */
    @FlowKey(defaultValue = "\"flag\"")
    private L<Boolean> flag = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) {
        o.output(flag, o.get(key).isPresent());
    }
}
