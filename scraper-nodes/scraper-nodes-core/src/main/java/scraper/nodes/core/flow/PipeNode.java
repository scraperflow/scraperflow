package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;

import java.util.List;

import static scraper.util.NodeUtil.addressOf;

/**
 * Pipe to goTo nodes and continue
 * @author Albert Schimpf
 */
@NodePlugin("1.0.0")
public final class PipeNode implements Node {

    /** List of goTo labels */
    @FlowKey(mandatory = true)
    private List<String> pipeTargets;

    @NotNull
    @Override
    public FlowMap process(NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {
        FlowMap output = o;

        for (String label : pipeTargets) {
            output = n.eval(output, addressOf(label));
        }

        return n.forward(output);
    }
}
