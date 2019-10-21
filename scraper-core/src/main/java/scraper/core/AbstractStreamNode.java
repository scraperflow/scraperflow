package scraper.core;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.NodeHook;
import scraper.api.node.type.StreamNode;
import scraper.util.NodeUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Fixes accept method for functional nodes
 */
@NodePlugin("1.0.0")
public abstract class AbstractStreamNode extends AbstractNode implements StreamNode {

    @FlowKey(defaultValue = "\"true\"")
    private @NotNull Boolean collect;

    /** Where the stream is dispatched */
    @FlowKey(mandatory = true)
    private @NotNull String streamTarget;

    private final @NotNull Map<UUID, FlowMap> openStreams = new HashMap<>();
    private final @NotNull Map<UUID, Map<String, List<Object>>> collectors = new HashMap<>();

    public void stream(FlowMap origin, FlowMap newMap, List<String> toCollect) {
        if(!collect) {
            // dispatch directly to stream target without collecting
            forkDispatch(newMap, NodeUtil.addressOf(streamTarget));
        } else {
            // create collector for origin ID
            openStreams.putIfAbsent(origin.getId(), NodeUtil.flowOf(origin));
            collectors.putIfAbsent(origin.getId(), new HashMap<>());

            toCollect.forEach(key -> {
                // get collector list for origin ID and key to be collected
                collectors.get(origin.getId()).putIfAbsent(key, new LinkedList<>());
                Map<String, List<Object>> collectorForId = collectors.get(origin.getId());
                // collect to list
                collectorForId.get(key).add(newMap.get(key));
            });
        }
    }

    @Override
    public Collection<NodeHook> beforeHooks() {
        NodeHook initCollectForFlow = o -> {
            if(collect) {
                log(NodeLogLevel.INFO, "Collecting stream for map {}", o.getId());
                openStreams.put(o.getId(), o);
                collectors.put(o.getId(), new HashMap<>());
            }
        };

        return Stream.concat(
                super.beforeHooks().stream(),
                Stream.of(initCollectForFlow)
        ).collect(Collectors.toList());
    }

    @Override
    public Collection<NodeHook> afterHooks() {
        NodeHook finishCollectForFlow = o -> {
            if(collect) {
                log(NodeLogLevel.INFO, "Finish collection for map {}", o.getId());
                FlowMap copy = NodeUtil.flowOf(openStreams.get(o.getId()));
                Map<String, List<Object>> toCollect = collectors.get(o.getId());
                toCollect.forEach(copy::put);
                forkDispatch(copy, NodeUtil.addressOf(streamTarget));

                openStreams.remove(o.getId());
                collectors.remove(o.getId());
            }
        };

        return Stream.concat(
                super.beforeHooks().stream(),
                Stream.of(finishCollectForFlow)
        ).collect(Collectors.toList());
    }
}
