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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Forks to targets and uses specified keys to join into the original flow.
 * Example:
 * <pre>
 * # retrieves the key fileinfo from the 'get-java-classes' target
 * # retrieves the key package from the 'get-source-info' target
 * type: ForkJoinNode
 * forkTargets: [get-java-classes, get-source-info]
 * targetToKeys:
 *   get-java-classes:
 *     fileinfo: fileinfo
 *   get-source-info:
 *     package: package
 * </pre>
 */
@NodePlugin("0.4.0")
public final class ForkJoinNode implements Node {

    /** Expected join for each target (for every key) defined in this map after a forked flow terminates */
    @FlowKey(mandatory = true)
    private final T<Map<String, Map<String, String>>> targetToKeys = new T<>(){};

    /** All processes to fork the current flow map to */
    @FlowKey(mandatory = true)
    private final T<List<Address>> forkTargets = new T<>(){};

    @NotNull @Override
    public FlowMap process(@NotNull final NodeContainer<? extends Node> n, @NotNull final FlowMap o) {
        Map<String, CompletableFuture<FlowMap>> forkedProcesses = new HashMap<>();
        o.evalIdentity(forkTargets).forEach(target -> {
            // dispatch new flow, expect future to return the modified flow map
            CompletableFuture<FlowMap> t = n.forkDepend(o, target);
            forkedProcesses.put(target.getRepresentation(), t);
        });

        forkedProcesses.forEach((adr, future) -> future.whenComplete(
                (result, throwable) ->
                        n.log(NodeLogLevel.DEBUG, "Fork complete")
        ));

        CompletableFuture
                .allOf(forkedProcesses.values().toArray(new CompletableFuture[0]))
                .join();

        forkedProcesses.forEach((target, future) ->{
            try {
                handleFuture(target, future, o, n);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                n.log(NodeLogLevel.ERROR, "Bad join", e);
            }
        });

        // continue
        return o;
    }

    private void handleFuture(String target, CompletableFuture<FlowMap> future, FlowMap o, NodeContainer<?> n) throws ExecutionException, InterruptedException {
        Map<String, String> keys = o.eval(this.targetToKeys).get(target);
        FlowMap forkedResult = future.get();

        keys.forEach((forked, main) -> {
            Optional<? super Object> result = forkedResult.get(forked);
            if(result.isEmpty()) {
                n.log(NodeLogLevel.ERROR, "Missing key '{}' in completed flow '{}'", forked, target);
                throw new TemplateException("Completed flow does not return expected key: "+ forked);
            }

            o.output(main, result.get());
        });
    }
}
