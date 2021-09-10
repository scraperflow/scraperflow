package scraper.test.plugins.core.typechecker.helper;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

/** Custom class extract node */
@NodePlugin(value = "0.0.1", deprecated = true)
public class TestRecExtract implements FunctionalNode {

    /** Get User Object */
    @FlowKey(mandatory = true)
    private final T<TestRec.RTestRec> get = new T<>(){};

    /** Location of name output */
    @FlowKey(defaultValue = "\"_\"")
    private final L<String> name = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        TestRec.RTestRec rec = o.eval(get);
        o.output(name, rec.name);
    }
}
