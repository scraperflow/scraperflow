package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeIOException;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.annotations.node.Flow;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.T;

import java.util.Map;

/**
 * Redirects flow depending on string evaluation
 */
@NodePlugin(value = "0.0.1", customFlowAfter = true)
public final class Redirect implements Node {

    /** Hostname to target label mapping, if any */
    @FlowKey(mandatory = true)
    @Flow(label = "")
    private final T<Map<String, Address>> redirectMap = new T<>(){};

    /** Redirect map */
    @FlowKey(defaultValue = "\"{redirect}\"")
    private final T<String> toRedirect = new T<>(){};

    @NotNull
    @Override
    public void process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        Map<String, Address> redirect = o.evalIdentity(redirectMap);
        String toRedirect = o.eval(this.toRedirect);

        if(!redirect.containsKey(toRedirect)) throw new NodeIOException("Redirect target not in map: " + toRedirect);

        n.forward(o, redirect.get(toRedirect));
    }
}
