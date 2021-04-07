package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

/**
 * Unzips a pair
 */
@NodePlugin("0.0.1")
public class UnzipSingle<A,B> implements FunctionalNode {

    /** zipped element */
    @FlowKey(mandatory = true)
    private final T<Zip<A,B>.Pair> pair = new T<>(){};

    /** unzip 1 */
    @FlowKey(mandatory = true)
    private final L<A> l1 = new L<>(){};

    /** unzip 2 */
    @FlowKey(mandatory = true)
    private final L<B> l2 = new L<>(){};


    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        Zip<A,B>.Pair unzipped = o.eval(this.pair);
        o.output(this.l1, unzipped.fst);
        o.output(this.l2, unzipped.snd);
    }
}
