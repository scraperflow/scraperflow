import scraper.annotations.*;
import scraper.api.*;
import scraper.nodes.core.flow.JoinKey;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Joins on a join key.
 */
@NodePlugin(value = "0.0.1")
@Stateful
public final class JoinCounts implements Node {

    /** Word count map to join */
    @FlowKey(mandatory = true)
    T<Map<String, Integer>> map = new T<>(){};

    /** Key which can be used to join flows. */
    @FlowKey
     T<JoinKey> joinKey = new T<>(){};

    /** Once all maps are joined the output count map is emitted */
    @FlowKey(mandatory = true)
    L<Map<String, Integer>> output = new L<>(){};

    // STATE
    final Lock l = new ReentrantLock();
    final Map<String, Integer> allcounts = new HashMap<>();
    final Map<JoinKey, Integer> joins = new HashMap<>();

    @Override
    public void process(NodeContainer<? extends Node> n, FlowMap o) {
        JoinKey joinKey = o.eval(this.joinKey);
        Map<String, Integer> counts = o.eval(map);
        Integer joins = this.joins.getOrDefault(joinKey, 0);

        l.lock();
        try {
            joins++;
            for (String key : counts.keySet()) {
                allcounts.merge(key, counts.get(key), Integer::sum);
            }

            if(joins == joinKey.size) {
                emit(o, n);
            }
            this.joins.put(joinKey, joins);
        } finally {
            l.unlock();
        }
    }

    private void emit(FlowMap evaluator, NodeContainer<? extends Node> n) {
        FlowMap toEmit = evaluator.copy();
        toEmit.output(output, allcounts);
        n.fork(toEmit);
    }
}
