package scraper.api.node.container;

import scraper.annotations.NotNull;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.type.StreamNode;

import java.util.List;

/**
 * @since 1.0.0
 */
public interface StreamNodeContainer extends NodeContainer<StreamNode> {
    void collect(@NotNull FlowMap o, @NotNull List<String> toCollect);

    void stream(@NotNull FlowMap origin,
                @NotNull FlowMap newMap) throws NodeException;

    FlowMap processStream(FlowMap o) throws NodeException;
}
