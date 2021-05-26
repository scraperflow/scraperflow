package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static java.util.AbstractMap.*;
import static java.util.stream.Collectors.*;

/**
 * Counts reply speed
 */
@NodePlugin("0.0.1")
public class ReplySpeed implements FunctionalNode {

    /** Replies in [ts :: number] format */
    @FlowKey(mandatory = true)
    private final T<List<String>> replies = new T<>(){};

    /** String output speed */
    @FlowKey(mandatory = true)
    private final L<String> speed = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        var replies = o.eval(this.replies);
        var groupedReplies = replies.stream()
                .collect(groupingBy(str ->
                        String.valueOf((Long.parseLong(str.split(" :: ")[0]) / 60000)))
                );

        Long minBucket = Collections.min(replies.stream().map(s -> Long.parseLong(s.split(" :: ")[0])).collect(toList()));
        Long maxBucket = Collections.max(replies.stream().map(s -> Long.parseLong(s.split(" :: ")[0])).collect(toList()));

        long lifetime = (maxBucket - minBucket) / 1000L / 60;
        int messages = Collections.max(replies.stream().map(s -> Integer.parseInt(s.split(" :: ")[1])).collect(toList()));

        if(lifetime == 0) o.output(speed, "0");
        else o.output(speed, String.valueOf(messages/(float) lifetime));
    }
}
