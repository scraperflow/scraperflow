package scraper.nodes.core.functional;

import scraper.annotations.*;
import scraper.api.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;


/**
 * Echoes current timestamp in milliseconds.
 */
@NodePlugin("0.1.0")
public final class Timestamp implements FunctionalNode {

    /** Where to put the timestamp */
    @FlowKey(mandatory = true)
    private final L<String> put = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        String timestamp = String.valueOf(Instant.ofEpochSecond(0L).until(Instant.now(), ChronoUnit.MILLIS));
        o.output(put, timestamp);
    }
}
