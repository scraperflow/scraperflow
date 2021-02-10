package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.Flow;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.container.NodeLogLevel;
import scraper.api.node.type.Node;
import scraper.api.template.T;
import scraper.util.TemplateUtil;

import java.util.List;

/**
 * Supplies target address with keys and returns with result keys.
 */
@NodePlugin(value = "0.0.1", customFlowAfter = true)
public final class Apply implements Node {

    /** Target of apply */
    @FlowKey(mandatory = true)
    @Flow(dependent = true, crossed = false, label = "apply")
    private Address applyTarget;

    /** Provided keys */
    @FlowKey(defaultValue = "[]")
    private final T<List<String>> supply = new T<>(){};

    /** Expected join for each key defined in this map after a forked flow terminates */
    @FlowKey(defaultValue = "{}")
    private final T<java.util.Map<String, String>> keys = new T<>(){};

    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {
        List<String> provided = o.evalIdentity(supply);

        FlowMap copy = o.copy();
        o.forEach((k, v) ->
                {
                    if( provided.contains(((String) k.getLocation().getRaw()))) {
                        // no op
                    } else {
                        copy.output(k, null);
                    }
                }
        );

        FlowMap returnMap = n.eval(copy, applyTarget);

        java.util.Map<String, String> keys = o.evalIdentity(this.keys);

        keys.forEach((joinKeyForked, joinKey) -> {
            n.log(NodeLogLevel.TRACE, "Joining {0} -> {1}", joinKeyForked, joinKey);

            Object forkedElement = returnMap.eval(TemplateUtil.templateOf(joinKeyForked));
            o.output(TemplateUtil.locationOf(joinKey), forkedElement);
        });

        // continue
        return o;
    }
}
