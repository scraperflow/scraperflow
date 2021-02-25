package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.LinkedList;
import java.util.List;

/**
 * Zips two lists, shortest length
 */
@NodePlugin("0.0.1")
public class ZipList<A,B> implements FunctionalNode {

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
