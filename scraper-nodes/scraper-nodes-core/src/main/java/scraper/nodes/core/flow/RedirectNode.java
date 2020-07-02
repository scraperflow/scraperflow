package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.T;

import java.util.Map;

/**
 * Redirects flow depending on string evaluation
 */
@NodePlugin("0.0.1")
public final class RedirectNode implements Node {

    /** Hostname to target label mapping, if any */
    @FlowKey(mandatory = true)
    private final T<Map<String, Address>> redirectMap = new T<>(){};

    /** Redirect map */
    @FlowKey(defaultValue = "\"{redirect}\"")
    private final T<String> toRedirect = new T<>(){};

    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {
        Map<String, Address> redirect = o.evalIdentity(redirectMap);
        String toRedirect = o.eval(this.toRedirect);

        if(!redirect.containsKey(toRedirect)) throw new NodeException("Redirect target not in map: " + toRedirect);

        return n.eval(o, redirect.get(toRedirect));
    }
}
