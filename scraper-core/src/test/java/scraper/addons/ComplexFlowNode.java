package scraper.addons;

import scraper.annotations.node.NodePlugin;
import scraper.api.flow.ControlFlowEdge;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.ControlFlowEdgeImpl;
import scraper.core.AbstractNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class ComplexFlowNode extends TestNode {
    @Override
    public FlowMap process(final FlowMap o) {return o;}

    public List<ControlFlowEdge> getOutput() {
        List<ControlFlowEdge> arr = new ArrayList<>(super.getOutput());


        { // dispatched edge
            ControlFlowEdge f = new ControlFlowEdgeImpl(nameOf("goTo"), "" + 3, "goTo", true);
            f.setDispatched(true);
            arr.add(f);
        }

        { // multi edge
            ControlFlowEdge f = new ControlFlowEdgeImpl(nameOf("goTo"), "" + 3, "goTo");
            f.setMultiple(true);
            arr.add(f);
        }

        return arr;
    }
}