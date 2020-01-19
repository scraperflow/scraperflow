package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.ControlFlowEdge;
import scraper.api.flow.impl.ControlFlowEdgeImpl;
import scraper.core.AbstractNode;
import scraper.api.flow.FlowMap;
import scraper.api.exceptions.NodeException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Override
    public FlowMap process(@NotNull final FlowMap o) throws NodeException {
        FlowMap output = o;

        for (String label : pipeTargets) {
            output = eval(output, addressOf(label));
        }

        return forward(output);
    }

    @Override
    public List<ControlFlowEdge> getOutput() {
        List<ControlFlowEdge> targets = new ArrayList<>();

        pipeTargets.forEach(target -> {
            ControlFlowEdge e = new ControlFlowEdgeImpl(getAddress(), getJobPojo().getNode(addressOf(target)).getAddress(), "pipe", false, false);
            targets.add(e);
        });

        return Stream.concat(super.getOutput().stream(), targets.stream()).collect(Collectors.toList());
    }
}
