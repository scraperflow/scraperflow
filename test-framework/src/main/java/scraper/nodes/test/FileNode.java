package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.node.EnsureFile;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.T;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class FileNode implements FunctionalNode {

    @EnsureFile
    @FlowKey(defaultValue = "\"/tmp/test-scraper-file\"")
    private String file;

    @EnsureFile
    @FlowKey(defaultValue = "\"/tmp/test-scraper-dir/\"")
    private String dir;

    @EnsureFile
    @FlowKey(defaultValue = "\"/tmp/test-scraper-dir/tree/tree/ok\"")
    private String dirtree;

    @EnsureFile
    @FlowKey
    private T<String> filet = new T<>() {};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) {}
}