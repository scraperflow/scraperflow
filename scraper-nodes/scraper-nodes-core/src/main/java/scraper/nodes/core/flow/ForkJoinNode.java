package scraper.nodes.core.flow;


import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.ControlFlowEdge;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.ControlFlowEdgeImpl;
import scraper.core.AbstractNode;
import scraper.core.NodeLogLevel;
import scraper.util.NodeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
@NodePlugin("0.1.0")
public final class ForkJoinNode extends AbstractNode {

    /** Expected join for each key defined in this map after a forked flow terminates */
    @FlowKey(mandatory = true)
    private Map<String, String> keys;

    /** All processes to fork the current flow map to */
    @FlowKey(mandatory = true)
    private List<String> forkTargets;

    @Override
    public FlowMap process(FlowMap o) throws NodeException {
        List<CompletableFuture<FlowMap>> forkedProcesses = new ArrayList<>();
        forkTargets.forEach(target -> {
            // dispatch new flow, expect future to return the modified flow map
            CompletableFuture<FlowMap> t = forkDepend(o, NodeUtil.addressOf(target));
            forkedProcesses.add(t);
        });

        forkedProcesses.forEach(future -> future.whenComplete(
                (result, throwable) ->
                        log(NodeLogLevel.INFO, "Fork complete")
        ));

        CompletableFuture
                .allOf(forkedProcesses.toArray(new CompletableFuture[0]))
                .join();


        keys.forEach((joinKeyForked, joinKey) -> {
            log(NodeLogLevel.INFO, "Joining {} -> {}", joinKeyForked, joinKey);

            //noinspection unchecked TODO nicer implementation
            List<Object> joinResults = (List<Object>) o.getOrDefault(joinKey, new ArrayList<>());

            forkedProcesses.forEach(future -> {
                try {
                    FlowMap fm = future.get();
                    joinResults.add(fm.get(joinKeyForked));
                    o.put(joinKey, joinResults);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    log(NodeLogLevel.ERROR, "Bad join", e);
                }
            });

        });


        log(NodeLogLevel.INFO, "JOINED!");

        // continue
        return forward(o);
    }

    @Override
    public List<ControlFlowEdge> getOutput() {
        return Stream.concat(
                super.getOutput().stream(),
                forkTargets.stream().map((Function<String, ControlFlowEdge>) target ->
                        new ControlFlowEdgeImpl(getAddress(), getJobPojo().getNode(NodeUtil.addressOf(target)).getAddress(), "forkJoin", false, true))
        ).collect(Collectors.toList());
    }
}
