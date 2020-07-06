package scraper.nodes.core.io;

import scraper.annotations.NotNull;
import scraper.annotations.node.EnsureFile;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.Io;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.T;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static scraper.api.node.container.NodeLogLevel.ERROR;

/**
 * Appends or writes a line to a file.
 */
@NodePlugin("0.1.0")
@Io
public final class WriteLineToFileNode implements FunctionalNode {

    /** Output file path */
    @FlowKey(mandatory = true) @EnsureFile
    private final T<String> output = new T<>(){};

    /** Line to be written to the file */
    @FlowKey(mandatory = true)
    private final T<String> line = new T<>(){};

    /** Overwrite output file or append */
    @FlowKey(defaultValue = "false")
    private Boolean overwrite;

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) throws NodeException {
        String content = o.eval(line);
        String output = o.eval(this.output);

        // TODO use file service instead
        try (PrintWriter fos = new PrintWriter(new FileOutputStream(output, !overwrite), true, StandardCharsets.ISO_8859_1)){
            if(!content.isEmpty()) fos.println(content);
        } catch (IOException e) {
            n.log(ERROR,"IO read error: {0}", e.getMessage());
            throw new NodeException(e, "IO read error, check system");
        }
    }
}