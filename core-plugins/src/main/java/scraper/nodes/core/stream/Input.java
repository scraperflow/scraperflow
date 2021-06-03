package scraper.nodes.core.stream;

import scraper.annotations.NotNull;
import scraper.annotations.Argument;
import scraper.annotations.FlowKey;
import scraper.annotations.Io;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.StreamNodeContainer;
import scraper.api.StreamNode;
import scraper.api.L;

import java.util.Scanner;

/**
 * Processes string input stream from the environment
 */
@NodePlugin("0.2.0")
@Io
public final class Input implements StreamNode {

    /** String output */
    @FlowKey(defaultValue = "\"_\"")
    private final L<String> put = new L<>(){};

    /** Token delimiter (default: any line break) */
    @FlowKey(defaultValue = "\"\\\\R\"") @Argument
    private String delimiter;

    /** End of stream delimiter, if any */
    @FlowKey
    private String endOfStreamDelimiter;

    @Override
    public void process(@NotNull final StreamNodeContainer n, @NotNull final FlowMap o) {
        Scanner s = new Scanner(System.in);
        s.useDelimiter(delimiter);
        s.tokens().forEach(t -> n.streamElement(o, put, t));
        if(endOfStreamDelimiter != null) n.streamElement(o, put, endOfStreamDelimiter);
    }
}
