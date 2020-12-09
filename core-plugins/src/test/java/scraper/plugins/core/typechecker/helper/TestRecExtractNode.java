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
public class TestRecExtractNode implements FunctionalNode {

    /** Get User Object */
    @FlowKey(mandatory = true)
    private final T<TestRecNode.TestRec> get = new T<>(){};

    /** Location of name output */
    @FlowKey(defaultValue = "\"_\"")
    private final L<String> name = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        TestRecNode.TestRec rec = o.eval(get);
        o.output(name, rec.name);
    }
}
