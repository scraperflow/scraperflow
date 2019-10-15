package scraper.addons;

import scraper.annotations.NotNull;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;

import static scraper.core.NodeLogLevel.INFO;

@NodePlugin(value = "1.2.3")
public final class SimplestNode extends AbstractFunctionalNode {
    @Override public void modify(@NotNull FlowMap o) { log(INFO, "pure"); }
}