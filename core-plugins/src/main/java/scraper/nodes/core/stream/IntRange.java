package scraper.nodes.core.stream;

import scraper.annotations.NotNull;
import scraper.annotations.Argument;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.StreamNodeContainer;
import scraper.api.StreamNode;
import scraper.api.L;
import scraper.nodes.core.flow.Fork;
import scraper.nodes.core.flow.JoinKey;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Int range generator
 * Example:
 * <pre>
 * type: IntRange
 * from: 1
 * to: 2
 * output: page
 * </pre>
 */
@NodePlugin("0.3.0")
public final class IntRange implements StreamNode {

    /** Start index, inclusive */
    @FlowKey(mandatory = true) @Argument
    private Integer from;

    /** End index, inclusive */
    @FlowKey(mandatory = true) @Argument
    private Integer to;

    /** Where the output will be put */
    @FlowKey(mandatory = true)
    private final L<Integer> output = new L<>(){};

    /** Key which can be used to join flows */
    @FlowKey(defaultValue = "\"_\"")
    private final L<JoinKey> joinKey = new L<>(){};


    @Override
    public void process(@NotNull final StreamNodeContainer n, @NotNull final FlowMap o) {
        AtomicInteger current = new AtomicInteger();
        int uid = new Random().nextInt();
        IntStream.rangeClosed(from, to).forEach(i ->{
            FlowMap copy = o.copy();
            copy.output(output, i);
            JoinKey key = new JoinKey(to, uid, current.getAndIncrement());
            copy.output(joinKey, key);
            n.streamFlowMap(o, copy);
        });
    }
}
