package scraper.addons;

import scraper.annotations.NotNull;
import scraper.annotations.node.EnsureFile;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;
import scraper.core.Template;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class FileNode extends AbstractFunctionalNode {

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
    private Template<String> filet = new Template<>() {};

    @Override
    public void modify(@NotNull final FlowMap o) {}
}