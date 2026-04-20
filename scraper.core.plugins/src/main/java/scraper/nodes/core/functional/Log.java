package scraper.nodes.core.functional;

import scraper.annotations.*;
import scraper.api.*;

/**
 * No-op node used just for logging.
 */
@NodePlugin("1.0.0")
public final class Log implements FunctionalNode {

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {}
}
