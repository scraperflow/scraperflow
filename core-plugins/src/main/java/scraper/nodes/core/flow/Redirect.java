package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeIOException;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.annotations.node.Flow;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.container.NodeLogLevel;
import scraper.api.node.type.Node;
import scraper.api.template.T;

import java.util.Map;

/**
 * Redirects flow depending on string evaluation.
 */
@NodePlugin(value = "0.0.2")
public final class Redirect implements Node {

    /** Cases to target addresses */
    @FlowKey(mandatory = true)
    @Flow(label = "")
    private final T<Map<String, Address>> redirectMap = new T<>(){};

    /** Redirect case */
    @FlowKey(mandatory = true)
    private final T<String> toRedirect = new T<>(){};

    @NotNull
    @Override
    public void process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        Map<String, Address> redirect = o.evalIdentity(redirectMap);
        String toRedirect = o.eval(this.toRedirect);

        if(!redirect.containsKey(toRedirect)) {
            String err = String.format("Redirect target not in map: %s", toRedirect);
            n.log(NodeLogLevel.ERROR, err);
            throw new NodeIOException(err);
        }

        n.forward(o, redirect.get(toRedirect));
    }
}
