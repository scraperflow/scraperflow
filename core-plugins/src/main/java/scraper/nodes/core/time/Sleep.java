package scraper.nodes.core.time;

import scraper.annotations.*;
import scraper.api.*;

/**
 * Sleeps a given amount of ms.
 */
@NodePlugin(value = "0.1.0")
@Stateful
public final class Sleep implements FunctionalNode {

    /** Sleep in ms */
    @FlowKey(mandatory = true)
    private final T<Integer> ms = new T<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        try {
            Thread.sleep(o.eval(ms));
        } catch (InterruptedException e) {
            throw new NodeException(e, "Sleep interrupted");
        }
    }
}
