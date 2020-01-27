package scraper.plugins.core.flowgraph.data;

import scraper.api.node.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.Version;

import java.util.Map;

public class ForkJoinNodeData {

    @Version("0.1.0")
    public static Map<String, String> getOutput(Map<String, String> previous, Node target, ScrapeInstance instance) throws Exception {
        // 0.1.0 has keys
        Map<String, String> keys = FlowUtil.getField("keys", target);

        for (String forkedKey : keys.keySet()) {
            previous.put(keys.get(forkedKey), "java.util.List<java.lang.Object>");
        }

        return previous;
    }
}
