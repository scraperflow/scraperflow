package scraper.nodes.io;

import scraper.annotations.node.EnsureFile;

import scraper.annotations.node.NodePlugin;
import scraper.annotations.node.FlowKey;
import scraper.api.flow.FlowMap;
import scraper.core.Template;
import scraper.core.AbstractNode;
import scraper.api.exceptions.NodeException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import static scraper.core.NodeLogLevel.ERROR;

/**
 * Appends or writes a line to a file.
 *
 * @author Albert Schimpf
 */
@NodePlugin("1.0.0")
public final class WriteLineToFileNode extends AbstractNode {

    /** Output file path */
    @FlowKey(mandatory = true) @EnsureFile
    private final Template<String> output = new Template<>(){};

    /** Line to be written to the file */
    @FlowKey(mandatory = true)
    private final Template<String> line = new Template<>(){};

    /** Overwrite output file or append */
    @FlowKey(defaultValue = "false")
    private Boolean overwrite;

    @Override
    public FlowMap process(final FlowMap o) throws NodeException {
        String content = line.eval(o);
        String output = this.output.eval(o);

        // TODO use file service instead
        try (PrintWriter fos = new PrintWriter(new FileOutputStream(output, !overwrite))){
            if(!content.isEmpty()) fos.println(content);
        } catch (IOException e) {
            log(ERROR,"IO read error: {}", e.getMessage());
            throw new NodeException(e, "IO read error, check system");
        }

        return forward(o);
    }
}