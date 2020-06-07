package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.container.NodeLogLevel;
import scraper.api.node.type.Node;
import scraper.api.template.T;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Applies a map to every element of a given list and joins the result keys into a list.
 * Example:
 * <pre>
 *
 * type: MapJoinNode
 * keys:
 *   package: package
 * list: "{output}"
 * putElement: "unzip-package-list"
 * mapTarget: unzip-package
 * ignoreMissingJoinKey: true
 * </pre>
 */
@NodePlugin("0.12.0")
public final class MapJoinNode <A> implements Node {

    /** Expected join for each key defined in this map after a forked flow terminates */
    @FlowKey(mandatory = true)
    private final T<Map<String, String>> keys = new T<>(){};

    /** List to apply map to */
    @FlowKey(mandatory = true)
    private final T<List<A>> list = new T<>(){};

    /** Label of goTo */
    @FlowKey(mandatory = true)
    private Address mapTarget;

    /** At which key to put the element of the list into. */
    @FlowKey(defaultValue = "\"element\"")
    private String putElement;

    /** Only distinct input elements */
    @FlowKey(defaultValue = "false")
    private Boolean distinct;

    /** Only distinct output elements */
    @FlowKey(defaultValue = "false")
    private Boolean distinctOutput;

    /** Skip missing join key elements */
    @FlowKey(defaultValue = "false")
    private Boolean ignoreMissingJoinKey;

    @NotNull @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        List<?> list = o.eval(this.list);
        if(distinct) list = new ArrayList<>(new HashSet<>(list));

        List<CompletableFuture<FlowMap>> forkedProcesses = new ArrayList<>();
        list.forEach(element -> {
            FlowMap copy = o.copy();
            copy.output(putElement, element);
            // dispatch new flow, expect future to return the modified flow map
            CompletableFuture<FlowMap> t = n.forkDepend(copy, mapTarget);
            forkedProcesses.add(t);
        });

        forkedProcesses.forEach(future ->
                future.whenComplete(
                (result, throwable) -> n.log(NodeLogLevel.DEBUG, "Map fork complete")
        ));

        CompletableFuture
                .allOf(forkedProcesses.toArray(new CompletableFuture[0]))
                .join();

        Map<String, String> keys = o.evalIdentity(this.keys);

        keys.forEach((joinKeyForked, joinKey) -> o.remove(joinKey));
        keys.forEach((joinKeyForked, joinKey) -> {
            n.log(NodeLogLevel.TRACE, "Joining {} -> {}", joinKeyForked, joinKey);

            List<? super Object> joinResults = o.evalMaybe(new T<List<? super Object>>() {}).orElse(new ArrayList<>());

            forkedProcesses.forEach(future -> {
                try {
                    FlowMap fm = future.get();
                    if(fm.get(joinKeyForked).isEmpty()) {
                        if(!ignoreMissingJoinKey)
                            throw new IllegalStateException(n.getAddress()+ ": Missing value at join key: " + joinKeyForked);
                    } else {
                        if (!joinResults.contains(fm.get(joinKeyForked).get()) || !distinctOutput) {
                            joinResults.add(fm.get(joinKeyForked).get());
                            o.output(joinKey, joinResults);
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    n.log(NodeLogLevel.ERROR, "Bad join", e);
                }
            });

        });
        keys.forEach((joinKeyForked, joinKey) -> { if(!o.keySet().contains(joinKey)) { o.output(joinKey, new ArrayList<>()); } });

        // continue
        return o;
    }
}
