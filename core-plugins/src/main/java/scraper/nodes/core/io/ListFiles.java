package scraper.nodes.core.io;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.Io;
import scraper.annotations.NodePlugin;
import scraper.api.NodeException;
import scraper.api.FlowMap;
import scraper.api.StreamNodeContainer;
import scraper.api.StreamNode;
import scraper.api.L;
import scraper.api.T;

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
