package scraper.nodes.core.time;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;


/**
 * Echoes current system timestamp in ms.
 */
@NodePlugin("0.2.0")
public final class Timestamp implements FunctionalNode {

    /** Result location */
    @FlowKey(mandatory = true)
    private final L<Long> put = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        o.output(put, System.currentTimeMillis());
    }
}
