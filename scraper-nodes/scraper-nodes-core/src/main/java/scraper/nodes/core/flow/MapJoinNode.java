package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractNode;
import scraper.core.NodeLogLevel;
import scraper.core.Template;
import scraper.util.NodeUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 *
 */
@NodePlugin("1.0.0")
public final class MapJoinNode extends AbstractNode {

    /** Expected join for each key defined in this map after a forked flow terminates */
    @FlowKey(mandatory = true)
    private Map<String, String> keys;

    /** List to apply map to */
    @FlowKey(mandatory = true) @NotNull
    private Template<List<Object>> list = new Template<>(){};

    /** Label of goTo */
    @FlowKey(mandatory = true)
    private String mapTarget;

    /** At which key to put the element of the list into. */
    @FlowKey(defaultValue = "\"element\"") @NotNull
    private Template<Object> putElement = new Template<>(){};

    /** Only distinct elements */
    @FlowKey(defaultValue = "false")
    private Boolean distinct;


    @NotNull
    @Override
    public FlowMap process(@NotNull final FlowMap o) throws NodeException {
        List<Object> list = this.list.eval(o);
        if(distinct) list = new ArrayList<>(new HashSet<>(list));

        List<CompletableFuture<FlowMap>> forkedProcesses = new ArrayList<>();
        list.forEach(element -> {
            FlowMap copy = NodeUtil.flowOf(o);
            putElement.output(copy, element);
            // dispatch new flow, expect future to return the modified flow map
            CompletableFuture<FlowMap> t = forkDepend(copy, NodeUtil.addressOf(mapTarget));
            forkedProcesses.add(t);
        });

//        forkedProcesses.forEach(future ->
//                future.whenComplete(
//                (result, throwable) -> log(NodeLogLevel.INFO, "Map fork complete")
//        ));

        CompletableFuture
                .allOf(forkedProcesses.toArray(new CompletableFuture[0]))
                .join();


        keys.forEach((joinKeyForked, joinKey) -> o.remove(joinKey));
        keys.forEach((joinKeyForked, joinKey) -> {
            log(NodeLogLevel.TRACE, "Joining {} -> {}", joinKeyForked, joinKey);

            //noinspection unchecked TODO nicer implementation
            List<Object> joinResults = (List<Object>) o.getOrDefault(joinKey, new ArrayList<>());

            forkedProcesses.forEach(future -> {
                try {
                    FlowMap fm = future.get();
                    if(fm.get(joinKeyForked)==null)
                        throw new IllegalStateException("Missing value at join key: " + joinKeyForked);
                    joinResults.add(fm.get(joinKeyForked));
                    o.put(joinKey, joinResults);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    log(NodeLogLevel.ERROR, "Bad join", e);
                }
            });

        });
        keys.forEach((joinKeyForked, joinKey) -> { if(!o.keySet().contains(joinKey)) { o.put(joinKey, new ArrayList<>()); } });

        // continue
        return forward(o);
    }
}
