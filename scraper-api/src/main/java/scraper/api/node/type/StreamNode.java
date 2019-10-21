package scraper.api.node.type;

import scraper.annotations.NotNull;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.Node;

import java.util.List;

/**
 * Nodes which implement this interface are able to stream (collect or emit) elements.
 * Elements are grouped by the origin id of a given FlowMap UUID.
 */
public interface StreamNode extends Node {
    void stream(@NotNull FlowMap origin,
                @NotNull FlowMap newMap,
                @NotNull List<String> collectKeys) throws NodeException;
}
