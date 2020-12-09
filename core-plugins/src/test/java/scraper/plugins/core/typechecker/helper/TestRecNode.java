package scraper.plugins.core.typechecker.helper;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

@NodePlugin(value = "0.0.1", deprecated = true)
public class TestRecNode implements FunctionalNode {
    @FlowKey(defaultValue = "\"_\"")
    private final L<TestRec> put = new L<>(){};
    @FlowKey(mandatory = true)
    private final T<String> name = new T<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        TestRec u = new TestRec();
        u.name = o.eval(name);
        o.output(put, u);
    }

    public static class TestRec {
        String name;
        public String toString() {
            return "{"+name+"}";
        }
    }
}
