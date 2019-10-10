package scraper.addons;

import scraper.annotations.NotNull;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.ControlFlowEdge;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.ControlFlowEdgeImpl;
import scraper.core.AbstractNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class ComplexFlowNode extends TestNode {
    @NotNull
    @Override
    public FlowMap process(@NotNull final FlowMap o) {return o;}

    @NotNull
    public List<ControlFlowEdge> getOutput() {
        List<ControlFlowEdge> arr = new ArrayList<>(super.getOutput());

        { // dispatched edge
            ControlFlowEdge f = new ControlFlowEdgeImpl(getAddress(), Objects.requireNonNull(getGoTo()), "goTo 3", false, true);
            arr.add(f);
        }

        { // multi edge
            ControlFlowEdge f = new ControlFlowEdgeImpl(getAddress(), Objects.requireNonNull(getGoTo()), "goTo 2", true, false);
            arr.add(f);
        }

        return arr;
    }
}