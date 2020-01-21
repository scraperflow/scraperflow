package scraper.plugins.core.flowgraph.api;

import scraper.annotations.NotNull;
import scraper.api.node.Address;

import java.util.Map;

/**
 * @since 1.0.0
 */
public interface DataFlowNode {
    /** @return address of this node */
    @NotNull Address getAddress();

    Map<String, String> consumes();
    Map<String, String> produces();
}
