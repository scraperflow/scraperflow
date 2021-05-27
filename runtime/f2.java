import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

@NodePlugin("1.0.0")
public class f2 implements FunctionalNode {

    @FlowKey private T<String> value = new T<>(){};

    @Override
    public void modify(FunctionalNodeContainer n, FlowMap o) {
        String x = o.eval(value);
        System.out.println(x);
        System.out.println("Hello");
    }
}
