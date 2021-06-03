package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;

import java.time.Instant;
import java.time.temporal.ChronoUnit;


/**
 * Echoes current timestamp
 */
@NodePlugin("0.1.0")
public final class Timestamp implements FunctionalNode {

    /** Where to put the timestamp */
    @FlowKey(defaultValue = "\"date\"") @NotNull
    private final L<String> put = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        String timestamp = String.valueOf(Instant.ofEpochSecond(0L).until(Instant.now(), ChronoUnit.MILLIS));
        o.output(put, timestamp);
    }
}
