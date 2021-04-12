package scraper.api.node.container;

import scraper.annotations.NotNull;
import scraper.api.flow.FlowMap;
import scraper.api.node.type.StreamNode;
import scraper.api.template.L;

/**
 * A container for stream nodes provides a node to collect and stream keys.
 *
 * The origin flow map is used to group requests for the collect case.
 */
public interface StreamNodeContainer extends NodeContainer<StreamNode> {

    /** Streams a single element match */
    <E> void streamElement(@NotNull FlowMap origin, @NotNull L<E> location, @NotNull E result);

    /** Streams a whole FlowMap match */
    void streamFlowMap(@NotNull FlowMap origin, @NotNull FlowMap result);

    /**
     * Process a stream which is used by the StreamNode to accept a FlowMap.
     * Wraps around the process accept method to ensure collecting matches or streaming them.
     */
    @NotNull FlowMap processStream(@NotNull FlowMap o);
}
