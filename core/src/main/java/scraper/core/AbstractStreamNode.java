package scraper.core;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.TemplateException;
import scraper.api.ValidationException;
import scraper.api.FlowMap;
import scraper.api.Address;
import scraper.annotations.Flow;
import scraper.api.NodeContainer;
import scraper.api.NodeLogLevel;
import scraper.api.StreamNodeContainer;
import scraper.api.StreamNode;
import scraper.api.ScrapeInstance;
import scraper.api.L;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static scraper.util.TemplateUtil.templateOf;


/**
 * Provides a streaming mechanism for a node.
 * Either collects results to a list or stream each individual element to a specified target.
 */
@SuppressWarnings({"rawtypes", "unchecked", "RedundantSuppression"}) // L raw types are checked statically
@NodePlugin(
        value = "0.2.0",
        customFlowAfter = true // convert output to list<X> instead of X if needed
)
public abstract class AbstractStreamNode extends AbstractNode<StreamNode> implements StreamNodeContainer {
    AbstractStreamNode(@NotNull String instance, @NotNull String graph, @Nullable String node, int index) { super(instance, graph, node, index); }

    /** If collect is disabled, this is the target a single stream element is streamed to */
    @FlowKey
    @Flow(label = "stream")
    private Address streamTarget;

    @Deprecated
    @FlowKey(defaultValue = "\"true\"")
    private Boolean collect;

    @Override
    public void init(@NotNull ScrapeInstance job) throws ValidationException {
        super.init(job);
    }

    private final @NotNull Map<UUID, FlowMap> openStreams = new ConcurrentHashMap<>();
    private final @NotNull Map<UUID, Map<L, List<Object>>> collectors = new ConcurrentHashMap<>();
    private final @NotNull Map<UUID, List<L<?>>> collectKeys = new ConcurrentHashMap<>();

    @Override
    public void streamFlowMap(@NotNull final FlowMap origin, @NotNull final FlowMap newMap) {
        stream(origin, newMap);
    }

    @Override
    public <E> void streamElement(@NotNull FlowMap origin, @NotNull L<E> location, @NotNull E result) {
        FlowMap newMap = origin.copy();
        newMap.output(location, result);

        stream(origin, newMap);
    }

    private void stream(@NotNull FlowMap origin, @NotNull FlowMap newMap) {
        if(streamTarget != null) {
            // dispatch directly to stream target without collecting
            forkDispatch(newMap, streamTarget);
        } else {
            collectKeys.get(origin.getId()).forEach(key -> {
                Map<L, List<Object>> collectorForId = collectors.get(origin.getId());
                // collect to list
                Optional<?> element = newMap.evalMaybe(templateOf(key));
                if(element.isEmpty()) {
                    log(NodeLogLevel.ERROR, "Missing expected element at key {0}, fix node implementation. Skipping", key.getLocation());
                    throw new TemplateException("Missing expected element at key " + key.getLocation());
                } else {
                    collectorForId.get(key).add(element.get());
                }
            });
        }
    }

    @NotNull
    @Override
    public void processStream(NodeContainer n, @NotNull final FlowMap o) {
        if(streamTarget == null) {
            log(NodeLogLevel.TRACE, "Collecting stream for map {0}", o.getId());
            // open stream for ID
            openStreams.put(o.getId(), o.copy());
            // open collectors for ID
            collectors.put(o.getId(), new HashMap<>());

            // collect these keys as default
            List<L<?>> toCollect = collectOutput();
            collectKeys.put(o.getId(), toCollect);
            // create empty list as default
            collectKeys.get(o.getId()).forEach(key -> collectors.get(o.getId()).put(key, new LinkedList<>()));
        }

        getC().process(this, o);

        if (streamTarget == null) {
            log(NodeLogLevel.TRACE, "Finish collection for map {0}", o.getId());
            FlowMap copy = openStreams.get(o.getId()).copy();
            Map<L, List<Object>> toCollect = collectors.get(o.getId());
            toCollect.forEach(copy::output);

            openStreams.remove(o.getId());
            collectors.remove(o.getId());
            collectKeys.remove(o.getId());

            n.forward(copy);
        }
    }

    private List<L<?>> collectOutput() {
        return Arrays.stream(getC().getClass().getDeclaredFields())
                .filter(f -> f.getType() == L.class)
                .map(f -> {
                    f.setAccessible(true);
                    try {
                        return (L<?>) f.get(getC());
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
    }
}
