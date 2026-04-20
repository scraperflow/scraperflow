package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.EnsureFile;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.T;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class File implements FunctionalNode {

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