package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;
import scraper.api.template.Term;
import scraper.util.TemplateUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Can scope the flow map.
 */
@NodePlugin("0.0.1")
public class Let<X> implements FunctionalNode {

    /** val */
    @FlowKey
    private final T<X> value = new T<>(){};

    @FlowKey
    private final L<X> key = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        X value = o.eval(this.value);
        o.output(key, value);
    }
}
