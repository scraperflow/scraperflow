package scraper.api.node.type;

import scraper.annotations.NotNull;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.container.StreamNodeContainer;

/**
 * Nodes which implement this interface are able to stream (collect or emit) elements.
 * Elements are grouped by the origin id of a given FlowMap.
 */
public interface StreamNode extends Node {
    /** Default accept method should only modify and forward the modified map */
    @NotNull
    @Override
    default FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull final FlowMap o) {
        assert n instanceof StreamNodeContainer;
        StreamNodeContainer sn = ((StreamNodeContainer) n);
        return sn.processStream(o);
    }

    void process(@NotNull StreamNodeContainer n, @NotNull FlowMap o);
}
