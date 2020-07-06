package scraper.core;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.exceptions.TemplateException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.api.node.container.NodeLogLevel;
import scraper.api.node.container.StreamNodeContainer;
import scraper.api.node.type.StreamNode;
import scraper.api.specification.ScrapeInstance;
import scraper.api.template.L;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Provides a streaming mechanism for a node.
 * Either collects results to a list or stream each individual element to a specified target.
 */
@NodePlugin("0.1.0")
public abstract class AbstractStreamNode extends AbstractNode<StreamNode> implements StreamNodeContainer {
    AbstractStreamNode(@NotNull String instance, @NotNull String graph, @Nullable String node, int index) { super(instance, graph, node, index); }

    /** If enabled, collects elements as a list without streaming */
    @FlowKey(defaultValue = "\"true\"")
    private Boolean collect;

    /** If collect is disabled, this is the target a single stream element is streamed to */
    @FlowKey
    private Address streamTarget;

    @Override
    public void init(@NotNull ScrapeInstance job) throws ValidationException {
        super.init(job);
        if(!collect)
            if(streamTarget == null) throw new ValidationException("Stream target has to be set for streaming mode");
    }

    private final @NotNull Map<UUID, FlowMap> openStreams = new ConcurrentHashMap<>();
    private final @NotNull Map<UUID, Map<String, List<Object>>> collectors = new ConcurrentHashMap<>();
    private final @NotNull Map<UUID, List<String>> collectKeys = new ConcurrentHashMap<>();

    @Override
    public void collect(@NotNull final FlowMap o, @NotNull final List<String> toCollect) {
        if(collect) {
            collectKeys.put(o.getId(), toCollect);
            // get collector list for origin ID and key to be collected
            collectors.put(o.getId(), new HashMap<>());
            
            // create collector for o ID
            openStreams.put(o.getId(), o.copy());

            // create empty list as default
            collectKeys.get(o.getId()).forEach(key -> collectors.get(o.getId()).put(key, new LinkedList<>()));
        }
    }

    @Override
    public void streamFlowMap(@NotNull final FlowMap origin, @NotNull final FlowMap newMap) {
        if(!collect) {
            // dispatch directly to stream target without collecting
            forkDispatch(newMap, streamTarget);
        } else {
            collectKeys.get(origin.getId()).forEach(key -> {
                Map<String, List<Object>> collectorForId = collectors.get(origin.getId());
                // collect to list
                Optional<?> element = newMap.get(key);
                if(element.isEmpty()) {
                    log(NodeLogLevel.ERROR, "Missing expected element at key {0}, fix node implementation. Skipping", key);
                    throw new TemplateException("Missing expected element at key " + key);
                } else {
                    collectorForId.get(key).add(element.get());
                }
            });
        }
    }

    @Override
    public <E> void streamElement(@NotNull FlowMap origin, @NotNull L<E> location, @NotNull E result) {
        FlowMap newMap = origin.copy();
        newMap.output(location, result);

        if(!collect) {
            // dispatch directly to stream target without collecting
            forkDispatch(newMap, streamTarget);
        } else {
            collectKeys.get(origin.getId()).forEach(key -> {
                Map<String, List<Object>> collectorForId = collectors.get(origin.getId());
                // collect to list
                Optional<?> element = newMap.get(key);
                if(element.isEmpty()) {
                    log(NodeLogLevel.ERROR, "Missing expected element at key {0}, fix node implementation. Skipping", key);
                    throw new TemplateException("Missing expected element at key " + key);
                } else {
                    collectorForId.get(key).add(element.get());
                }
            });
        }

    }

    @NotNull
    @Override
    public FlowMap processStream(@NotNull final FlowMap o) throws NodeException {
        if(collect) {
            log(NodeLogLevel.TRACE, "Collecting stream for map {0}", o.getId());
            openStreams.put(o.getId(), o);
            collectors.put(o.getId(), new HashMap<>());
        }

        getC().process(this, o);

        if(!collect) {
            return o;
        } else {
            log(NodeLogLevel.TRACE, "Finish collection for map {0}", o.getId());
            FlowMap copy = openStreams.get(o.getId()).copy();
            Map<String, List<Object>> toCollect = collectors.get(o.getId());
            toCollect.forEach(copy::output);

            openStreams.remove(o.getId());
            collectors.remove(o.getId());
            collectKeys.remove(o.getId());

            return copy;
        }
    }
}
