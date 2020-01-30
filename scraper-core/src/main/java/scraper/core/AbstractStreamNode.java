package scraper.core;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.api.node.container.NodeLogLevel;
import scraper.api.node.container.StreamNodeContainer;
import scraper.api.node.type.StreamNode;
import scraper.api.specification.ScrapeInstance;
import scraper.util.NodeUtil;

import java.util.*;


/**
 * Fixes accept method for functional nodes
 */
@NodePlugin("1.0.0")
public abstract class AbstractStreamNode extends AbstractNode<StreamNode> implements StreamNodeContainer {
    @Override
    public void init(@NotNull ScrapeInstance job) throws ValidationException {
        super.init(job);
        if(!collect)
            if(streamTarget == null) throw new ValidationException("Stream target has to be set for streaming mode");
    }

    @FlowKey(defaultValue = "\"true\"")
    private Boolean collect;

    /** Where the stream is dispatched */
    @FlowKey private Address streamTarget;

    private final @NotNull Map<UUID, FlowMap> openStreams = new HashMap<>();
    private final @NotNull Map<UUID, Map<String, List<Object>>> collectors = new HashMap<>();
    private final @NotNull Map<UUID, List<String>> collectKeys = new HashMap<>();

    public void collect(@NotNull FlowMap o, @NotNull List<String> toCollect) {
        if(collect) {
            collectKeys.put(o.getId(), toCollect);
            // get collector list for origin ID and key to be collected
            collectors.put(o.getId(), new HashMap<>());
            
            // create collector for o ID
            openStreams.put(o.getId(), NodeUtil.flowOf(o));

            // create empty list as default
            collectKeys.get(o.getId()).forEach(key -> collectors.get(o.getId()).put(key, new LinkedList<>()));
        }
    }

    public void stream(@NotNull FlowMap origin, @NotNull FlowMap newMap) {
        if(!collect) {
            // dispatch directly to stream target without collecting
            forkDispatch(newMap, streamTarget);
        } else {
            collectKeys.get(origin.getId()).forEach(key -> {
                Map<String, List<Object>> collectorForId = collectors.get(origin.getId());
                // collect to list
                collectorForId.get(key).add(newMap.get(key));
            });
        }
    }

    @Override
    public FlowMap processStream(FlowMap o) throws NodeException {
        if(collect) {
            log(NodeLogLevel.TRACE, "Collecting stream for map {}", o.getId());
            openStreams.put(o.getId(), o);
            collectors.put(o.getId(), new HashMap<>());
        }

        getC().process(this, o);

        if(!collect) {
            return forward(o);
        } else {
            log(NodeLogLevel.TRACE, "Finish collection for map {}", o.getId());
            FlowMap copy = NodeUtil.flowOf(openStreams.get(o.getId()));
            Map<String, List<Object>> toCollect = collectors.get(o.getId());
            toCollect.forEach(copy::put);

            openStreams.remove(o.getId());
            collectors.remove(o.getId());
            collectKeys.remove(o.getId());

            return forward(copy);
        }
    }
}
