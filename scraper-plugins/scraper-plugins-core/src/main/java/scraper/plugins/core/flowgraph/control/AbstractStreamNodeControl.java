package scraper.plugins.core.flowgraph.control;


import scraper.annotations.NotNull;
import scraper.api.flow.impl.IdentityFlowMap;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.reflect.T;
import scraper.api.specification.ScrapeInstance;
import scraper.core.AbstractStreamNode;
import scraper.core.Template;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.Version;
import scraper.util.NodeUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static scraper.plugins.core.flowgraph.impl.ControlFlowEdgeImpl.edge;

/**
 *
 */
public final class AbstractStreamNodeControl {

    @Version("0.1.0") @NotNull
    public static List<ControlFlowEdge> getOutput(List<ControlFlowEdge> previous, NodeContainer<? extends Node> node, ScrapeInstance spec) throws Exception {
        //noinspection OptionalGetWithoutIsPresent 0.1.0 has collect
        Boolean collect = (Boolean) FlowUtil.getFieldForClass("collect", node, AbstractStreamNode.class).get();
        if(collect) { // forward contract
            return previous;
        } else {
            //noinspection OptionalGetWithoutIsPresent 0.1.0 has streamTarget, mandatory if collect true
            Address streamT = (Address) FlowUtil.getFieldForClass("streamTarget", node, AbstractStreamNode.class).get();
            NodeContainer<? extends Node> streamTarget = NodeUtil.getTarget(node.getAddress(), streamT, spec);


            return Stream.concat(
                    // remove the forward contract edge
                    previous.stream().filter(e -> !e.getDisplayLabel().equalsIgnoreCase("forward")),
                    Stream.of(edge(node.getAddress(), streamTarget.getAddress(), "stream", true, true))
            ).collect(Collectors.toList());
        }
    }

}

