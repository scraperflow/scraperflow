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
 * Unzips
 */
@NodePlugin("0.0.1")
public class UnzipSingle<A,B> implements FunctionalNode {

    /** zipped list */
    @FlowKey(mandatory = true)
    private final T<ZipList<A,B>.Pair> pair = new T<>(){};

    /** unzip list 1 */
    @FlowKey(mandatory = true)
    private final L<A> l1 = new L<>(){};

    /** unzip list 2 */
    @FlowKey(mandatory = true)
    private final L<B> l2 = new L<>(){};


    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        ZipList<A,B>.Pair zipped = o.eval(this.pair);

        o.output(this.l1, zipped.fst);
        o.output(this.l2, zipped.snd);
    }
}
