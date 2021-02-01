package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.annotations.node.Flow;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.container.NodeLogLevel;
import scraper.api.node.type.Node;
import scraper.api.template.L;
import scraper.api.template.T;
import scraper.util.TemplateUtil;

import java.util.*;
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
@NodePlugin(value = "0.13.0", customFlowAfter = true)
public final class MapJoin <A> implements Node {

    /** Expected join for each key defined in this map after a forked flow terminates */
    @FlowKey(mandatory = true)
    private final T<java.util.Map<String, String>> keys = new T<>(){};

    /** List to apply map to */
    @FlowKey(mandatory = true)
    private final T<List<A>> list = new T<>(){};

    /** Label of goTo */
    @FlowKey(mandatory = true)
    @Flow(dependent = true, crossed = true, label = "map")
    private Address mapTarget;

    /** At which key to put the element of the list into. */
    @FlowKey(defaultValue = "\"_\"")
    private final L<A> putElement = new L<>(){};

    /** Only distinct input elements */
    @FlowKey(defaultValue = "false")
    private Boolean distinct;

    /** Only distinct output elements */
    @FlowKey(defaultValue = "false")
    private Boolean distinctOutput;

    /** Skip missing join key elements */
    @FlowKey(defaultValue = "false")
    private Boolean ignoreMissingJoinKey;

    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        List<A> list = o.eval(this.list);
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

        java.util.Map<String, String> keys = o.evalIdentity(this.keys);

        keys.forEach((joinKeyForked, joinKey) -> {
            n.log(NodeLogLevel.TRACE, "Joining {0} -> {1}", joinKeyForked, joinKey);

            List<Object> joinResults = o.evalMaybe(new T<List<Object>>() {}).orElse(new ArrayList<>());

            forkedProcesses.forEach(future -> {
                try {
                    FlowMap fm = future.get();
                    Optional<?> forkedElement = fm.evalMaybe(TemplateUtil.templateOf(joinKeyForked));
                    if(forkedElement.isEmpty()) {
                        if(!ignoreMissingJoinKey)
                            throw new IllegalStateException(n.getAddress()+ ": Missing value at join key: " + joinKeyForked);
                    } else {
                        if (!joinResults.contains(forkedElement.get()) || !distinctOutput) {
                            joinResults.add(forkedElement.get());
                            o.output(TemplateUtil.locationOf(joinKey), joinResults);
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    n.log(NodeLogLevel.ERROR, "Bad join", e);
                }
            });

        });
        // default output for empty collection
        keys.forEach((joinKeyForked, joinKey) -> { if(o.evalMaybe(TemplateUtil.templateOf(joinKey)).isEmpty()) {
            o.output(TemplateUtil.locationOf(joinKey), new ArrayList<>());
        } });

        // continue
        return o;
    }
}
