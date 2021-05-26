package scraper.nodes.core.io;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.Io;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.StreamNodeContainer;
import scraper.api.node.type.StreamNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.io.File;

/**
 * List files in a given directory.
 * Can filter for directories only
 */
@NodePlugin("0.1.0")
@Io
public final class ListFiles implements StreamNode {

    /** List files of this directory */
    @FlowKey(defaultValue = "\"{file}\"")
    private final T<String> file = new T<>(){};

    /** Where the output filename will be put. */
    @FlowKey(defaultValue = "\"filename\"")
    private final L<String> filename = new L<>(){};

    @FlowKey(defaultValue = "false")
    private Boolean onlyDirectories;

    @Override
    public void process(@NotNull StreamNodeContainer n, @NotNull FlowMap o) throws NodeException {

        try {
            for (File listFile : new File(o.eval(file)).listFiles()) {
                if(onlyDirectories && !listFile.isDirectory()) continue;
                n.streamElement(o, filename, listFile.getName());
            }
        } catch (Exception e){
            throw new NodeException(e, "Could not list files");
        }
    }
}
