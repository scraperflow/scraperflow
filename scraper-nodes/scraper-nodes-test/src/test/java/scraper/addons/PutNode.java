package scraper.addons;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;

@NodePlugin(value = "1.2.3")
public final class PutNode extends AbstractFunctionalNode {
    @FlowKey private String toPut;
    @Override public void modify(@NotNull FlowMap o) { o.put("hello", "world"); o.put(toPut, "result"); };
}