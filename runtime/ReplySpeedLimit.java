package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Counts reply speed
 */
@NodePlugin("0.0.1")
public class ReplySpeedLimit implements FunctionalNode {

    /** Replies in [ts :: number] format */
    @FlowKey(mandatory = true)
    private final T<List<String>> replies = new T<>(){};

    /** Timelimit in last minutes */
    @FlowKey(mandatory = true)
    private final T<Integer> limit = new T<>(){};

    /** String output speed */
    @FlowKey(mandatory = true)
    private final L<String> speed = new L<>(){};


    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        var replies = o.eval(this.replies);
//        var groupedReplies = replies.stream()
//                .collect(groupingBy(str ->
//                        String.valueOf((Long.parseLong(str.split(" :: ")[0]) / 60000)))
//                );

        Instant minBucket = Collections.min(replies.stream().map(s -> Instant.ofEpochMilli(Long.parseLong(s.split(" :: ")[0]))).collect(toList()));
        Instant maxBucket = Collections.max(replies.stream().map(s -> Instant.ofEpochMilli(Long.parseLong(s.split(" :: ")[0]))).collect(toList()));

        Instant lim = maxBucket.minus(o.eval(limit), ChronoUnit.MINUTES);
        Instant cutoff;
        if(lim.isBefore(minBucket)) {
            cutoff = minBucket;
        } else {
            cutoff = lim;
        }

        var messages = (replies.stream()
                .filter(s -> Instant.ofEpochMilli(Long.parseLong(s.split(" :: ")[0])).isAfter(cutoff))
                .collect(toList()));

        var realMessages = Integer.parseInt(messages.get(messages.size()-1).split(" :: ")[1])
                - Integer.parseInt(messages.get(0).split(" :: ")[1]);

        Duration lifetime = Duration.between(maxBucket, cutoff);

        if(Math.abs(lifetime.toMinutes()) <= 0) { o.output(speed, "0"); }
        else {
            String speedstr = String.valueOf(realMessages/(double) Math.abs(lifetime.toMinutes()));
            o.output(speed, speedstr);
        }
    }
}
