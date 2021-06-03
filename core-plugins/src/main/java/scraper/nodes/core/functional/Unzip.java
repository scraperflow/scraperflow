package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Unzips a list of pairs
 */
@NodePlugin("0.0.1")
public class Unzip<A,B> implements FunctionalNode {

    /** zipped list */
    @FlowKey(mandatory = true)
    private final T<List<Zip<A,B>.Pair>> pair = new T<>(){};

    /** unzip list 1 */
    @FlowKey(mandatory = true)
    private final L<List<A>> l1 = new L<>(){};

    /** unzip list 2 */
    @FlowKey(mandatory = true)
    private final L<List<B>> l2 = new L<>(){};


    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        List<Zip<A,B>.Pair> unzipped = o.eval(this.pair);
        List<A> first = unzipped.stream().map(z -> z.fst).collect(Collectors.toList());
        List<B> second = unzipped.stream().map(z -> z.snd).collect(Collectors.toList());
        o.output(this.l1, first);
        o.output(this.l2, second);
    }
}
