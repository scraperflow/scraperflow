package scraper.plugins.core.flowgraph.data;

import scraper.api.node.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.core.Template;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.Version;

import java.util.List;
import java.util.Map;

public class EchoNodeData  {

    @Version("1.1.0")
    public static Map<String, String> getOutput(Map<String, String> previous, Node target, ScrapeInstance instance) throws Exception {
        // 1.1.0 has puts and remove
        Template<Map<String, Object>> puts = FlowUtil.getField("puts", target);
        List<String> remove = FlowUtil.getField("remove", target);

        // this assumes that no template are used in puts keys
        for (String toPut : puts.evalWithIdentity().keySet()) {
            // TODO more than just 'Object' can be inferred about the type here
            previous.put(toPut, "scraper.core.Template<java.lang.Object>");
        }

        // remove entries
        for (String toRemove : remove) {
            previous.remove(toRemove);
        }

        return previous;
    }
}
