package scraper.nodes.core.complex;

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
 * Only keys that are defined in <var>keys</var> are kept, others are deleted.
 */
@NodePlugin(value = "0.0.1", customFlowAfter = true)
public class LetIn implements FunctionalNode {

    /** Keys to include in following scope */
    @FlowKey
    private final T<List<String>> keys = new T<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        List<String> whitelist = o.eval(keys);

        // filter
        List<Term<?>> locationsToRemove = new LinkedList<>();
        o.forEach((l,v) -> {
            if(!whitelist.contains(l.getLocation().getRaw().toString()))
                locationsToRemove.add(l.getLocation());
        });

        // remove
        locationsToRemove.forEach(term -> {
            L<?> loc = TemplateUtil.locationOf(term.getRaw().toString());
            o.output(loc, null);
        });
    }
}
