package scraper.nodes.core.flow;


import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.ControlFlowEdge;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.ControlFlowEdgeImpl;
import scraper.core.AbstractNode;
import scraper.util.NodeUtil;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
@NodePlugin("0.1.0")
public final class ForkNode extends AbstractNode {

    /** All nodes to fork the current flow map to */
    @FlowKey(mandatory = true)
    private List<String> forkTargets;

    @Override
    public FlowMap process(FlowMap o) throws NodeException {
        forkTargets.forEach(target -> {
            // dispatch new flow for every goTo
            FlowMap copy = NodeUtil.flowOf(o);
            forkDispatch(copy, NodeUtil.addressOf(target));
        });

        return forward(o);
    }

    @Override
    public List<ControlFlowEdge> getOutput() {
        return Stream.concat(
                super.getOutput().stream(),
                forkTargets.stream().map((Function<String, ControlFlowEdge>) target ->
                        new ControlFlowEdgeImpl(getAddress(), getJobPojo().getNode(NodeUtil.addressOf(target)).getAddress(), "fork", false, true))
        ).collect(Collectors.toList());
    }
}
