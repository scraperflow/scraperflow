package scraper.nodes.apptest;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;

import static scraper.api.node.container.NodeLogLevel.ERROR;


/**
 * @author Albert Schimpf
 */
@NodePlugin(value = "0.0.1", deprecated = true)
public final class NoNode implements Node {

    @FlowKey(defaultValue = "false")
    private Boolean fail;

    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {
        System.setProperty("done", "true");
        if(fail) {
            n.log(ERROR,"EXP");
            throw new NodeException("abc");
        }

        return o;
    }
}
