package scraper.nodes.core.io;

import scraper.annotations.NotNull;
import scraper.annotations.EnsureFile;
import scraper.annotations.FlowKey;
import scraper.annotations.Io;
import scraper.annotations.NodePlugin;
import scraper.api.NodeIOException;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.NodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.Node;
import scraper.api.ScrapeInstance;
import scraper.api.T;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import static scraper.api.NodeLogLevel.ERROR;

/**
 * Appends or writes a line to a file.
 */
@NodePlugin("0.1.1")
@Io
public final class WriteLineToFile implements FunctionalNode {

    /** Output file path */
    @FlowKey(mandatory = true) @EnsureFile
    private final T<String> output = new T<>(){};

    /** Line to be written to the file */
    @FlowKey(mandatory = true)
    private final T<String> line = new T<>(){};

    /** Overwrite output file or append */
    @FlowKey(defaultValue = "false")
    private Boolean overwrite;

    /** charset */
    @FlowKey(defaultValue = "\"UTF-8\"")
    private String charset;

    @Override
    public void init(NodeContainer<? extends Node> n, ScrapeInstance instance) { Charset.forName(charset); }

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) {
        String content = o.eval(line);
        String output = o.eval(this.output);

        // TODO use file service instead
        try (PrintWriter fos = new PrintWriter(new FileOutputStream(output, !overwrite), true, Charset.forName(charset))){
            if(!content.isEmpty()) fos.println(content);
        } catch (IOException e) {
            n.log(ERROR,"IO read error: {0}", e.getMessage());
            throw new NodeIOException(e, "IO read error, check system");
        }
    }
}