package scraper.nodes.core.io;

import scraper.annotations.*;
import scraper.api.*;

import java.io.File;

/**
 * List files in a given directory.
 * Can filter for directories only
 */
@NodePlugin("0.2.0")
@Io
public final class ListFiles implements StreamNode {

    /** List files of this directory */
    @FlowKey(mandatory = true)
    private final T<String> file = new T<>(){};

    @FlowKey(defaultValue = "false")
    private Boolean onlyDirectories;

    /** Where the output filename will be put. */
    @FlowKey(mandatory = true)
    private final L<String> filename = new L<>(){};

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
