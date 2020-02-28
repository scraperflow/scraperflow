package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.container.NodeLogLevel;
import scraper.api.node.type.Node;
import scraper.api.template.T;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 *
 */
@NodePlugin("0.2.0")
public final class ForkJoinNode implements Node {

    /** Expected join for each key defined in this map after a forked flow terminates */
    @FlowKey(mandatory = true)
    private T<Map<String, String>> keys = new T<>(){};

    /** All processes to fork the current flow map to */
    @FlowKey(mandatory = true)
    private T<List<Address>> forkTargets = new T<>(){};

    @NotNull @Override
    public FlowMap process(@NotNull final NodeContainer<? extends Node> n, @NotNull final FlowMap o) {
        Map<String, String> keys = o.evalIdentity(this.keys);

        List<CompletableFuture<FlowMap>> forkedProcesses = new ArrayList<>();
        o.evalIdentity(forkTargets).forEach(target -> {
            // dispatch new flow, expect future to return the modified flow map
            CompletableFuture<FlowMap> t = n.forkDepend(o, target);
            forkedProcesses.add(t);
        });

        forkedProcesses.forEach(future -> future.whenComplete(
                (result, throwable) ->
                        n.log(NodeLogLevel.DEBUG, "Fork complete")
        ));

        CompletableFuture
                .allOf(forkedProcesses.toArray(new CompletableFuture[0]))
                .join();


        keys.forEach((joinKeyForked, joinKey) -> {
            n.log(NodeLogLevel.DEBUG, "Joining {} -> {}", joinKeyForked, joinKey);

            List<Object> joinResults = o.evalMaybe(new T<List<Object>>() {}).orElse(new ArrayList<>());

            forkedProcesses.forEach(future -> {
                try {
                    FlowMap fm = future.get();
                    Optional<?> returnResult = fm.get(joinKeyForked);
                    if(returnResult.isEmpty()) {
                        n.log(NodeLogLevel.ERROR, "Missing key in completed flow {}", joinKeyForked);
                        throw new TemplateException("Completed flow does not return expected key: "+ joinKeyForked);
                    } else {
                        joinResults.add(returnResult.get());
                        o.output(joinKey, joinResults);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    n.log(NodeLogLevel.ERROR, "Bad join", e);
                }
            });

        });

        // continue
        return o;
    }

}
