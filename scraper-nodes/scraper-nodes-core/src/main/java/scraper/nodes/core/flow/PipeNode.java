package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractNode;

import java.util.List;

import static scraper.util.NodeUtil.addressOf;

/**
 * Pipe to goTo nodes and continue
 * @author Albert Schimpf
 */
@NodePlugin("1.0.0")
public final class PipeNode extends AbstractNode {

    /** List of goTo labels */
    @FlowKey(mandatory = true)
    private List<String> pipeTargets;

    @NotNull
    @Override
    public FlowMap process(@NotNull final FlowMap o) throws NodeException {
        FlowMap output = o;

        for (String label : pipeTargets) {
            output = eval(output, addressOf(label));
        }

        return forward(output);
    }
}
