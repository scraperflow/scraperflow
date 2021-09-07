package scraper.plugins.core.typechecker.helper;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

@NodePlugin(value = "0.0.1", deprecated = true)
public class TestRec implements FunctionalNode {
    @FlowKey(defaultValue = "\"_\"")
    private final L<RTestRec> put = new L<>(){};
    @FlowKey(mandatory = true)
    private final T<String> name = new T<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        RTestRec u = new RTestRec();
        u.name = o.eval(name);
        o.output(put, u);
    }

    public static class RTestRec {
        String name;
        public String toString() {
            return "{"+name+"}";
        }
    }
}
