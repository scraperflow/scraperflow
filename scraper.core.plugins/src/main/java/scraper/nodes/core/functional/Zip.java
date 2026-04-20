package scraper.nodes.core.functional;

import scraper.annotations.*;
import scraper.api.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Zips two lists, shortest length wins
 */
@NodePlugin("0.0.1")
public class Zip<A,B> implements FunctionalNode {

    /** list 1 */
    @FlowKey(mandatory = true)
    private final T<List<A>> l1 = new T<>(){};

    /** list 2 */
    @FlowKey(mandatory = true)
    private final T<List<B>> l2 = new T<>(){};

    /** Integer Output */
    @FlowKey(mandatory = true)
    private final L<List<Pair>> result = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        List<A> l1 = o.eval(this.l1);
        List<B> l2 = o.eval(this.l2);

        List<Pair> zipped = new LinkedList<>();
        for (int i = 0; i < Math.min(l1.size(), l2.size()); i++) {
            Pair x = new Pair();
            x.fst = l1.get(i);
            x.snd = l2.get(i);
            zipped.add(x);
        }

        o.output(result, zipped);
    }

    class Pair {
        A fst;
        B snd;
        @Override
        public String toString() {
            return "("+fst.toString() +"," +snd.toString() +")";
        }
    }
}
