package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.T;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Can modify the current flow map.
 * Can be used to construct user defined JSON objects.
 * <p>
 * Example
 * <pre>
 * type: EchoNode
 * puts:
 *   id: "{id}"
 *   body: "{parsed-body}"
 * </pre>
 */
@NodePlugin("1.2.1")
public class EchoNode implements FunctionalNode {

    /** Multiple put operations can be specified in this map at once */
    @FlowKey(defaultValue = "{}")
    private final T<Map<String, ?>> puts = new T<>(){};

    /** All keys specified in this list will be removed from the FlowMap */
    @FlowKey(defaultValue = "[]")
    private T<List<String>> remove = new T<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        Optional<Map<String, ?>> puts = o.evalMaybe(this.puts);
        List<String> remove = o.evalIdentity(this.remove);

        // put multiple objects/strings
        if(puts.isPresent()) for (String key : puts.get().keySet()) {
            o.output(key, puts.get().get(key));
        }

        // remove keys
        for (String key : remove) {
            o.remove(key);
        }
    }
}
