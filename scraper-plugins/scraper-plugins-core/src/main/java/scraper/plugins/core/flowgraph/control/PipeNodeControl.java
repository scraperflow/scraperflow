package scraper.plugins.core.flowgraph.control;


import scraper.annotations.NotNull;
import scraper.api.flow.impl.IdentityFlowMap;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.reflect.T;
import scraper.api.specification.ScrapeInstance;
import scraper.core.Template;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.Version;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static scraper.plugins.core.flowgraph.impl.ControlFlowEdgeImpl.edge;

public final class PipeNodeControl {
    @Version("1.0.0") @NotNull
    public static List<ControlFlowEdge> getOutput(List<ControlFlowEdge> previous, NodeContainer node, ScrapeInstance spec) throws Exception {
        //noinspection unchecked, OptionalGetWithoutIsPresent 1.0.0 has pipeTargets, mandatory
        List<Address> pipeTargets = Template.eval((T<List<Address>>) FlowUtil.getField("pipeTargets", node.getC()).get(), new IdentityFlowMap());

        return Stream.concat(
                previous.stream(),
                IntStream.rangeClosed(0, pipeTargets.size()-1).mapToObj(i ->
                        edge(node.getAddress(), pipeTargets.get(i), "pipe@"+i)
                )
        ).collect(Collectors.toList());
    }
}

// old api version reference
//    public List<ControlFlowEdge> getOutput() {
//        List<ControlFlowEdge> targets = new ArrayList<>();
//
//        pipeTargets.forEach(target -> {
//            ControlFlowEdge e = new ControlFlowEdgeImpl(getAddress(), getJobPojo().getNode(addressOf(target)).getAddress(), "pipe", false, false);
//            targets.add(e);
//        });
//
//        return Stream.concat(super.getOutput().stream(), targets.stream()).collect(Collectors.toList());
//    }
