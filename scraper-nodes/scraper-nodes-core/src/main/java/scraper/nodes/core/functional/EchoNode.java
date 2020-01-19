package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;
import scraper.core.Template;

import java.util.List;
import java.util.Map;

/**
 * Can modify the current argument map.
 */
@NodePlugin("1.0.0")
public final class EchoNode extends AbstractFunctionalNode {

    /** Multiple put operations can be specified in this map at once */
    @FlowKey(defaultValue = "{}") @NotNull
    private final Template<Map<String, Object>> puts = new Template<>(){};

    /** All keys specified in this list will be removed from the FlowMap */
    @FlowKey(defaultValue = "[]") @NotNull
    private final Template<List<String>> remove = new Template<>(){};

    @Override
    public void modify(@NotNull final FlowMap o) {
        Map<String, Object> puts = this.puts.eval(o);
        List<String> remove = this.remove.eval(o);

        // put multiple objects/strings
        for (String key : puts.keySet()) {
            o.put(key, puts.get(key));
        }

        // remove keys
        for (String key : remove) {
            o.remove(key);
        }
    }
}
