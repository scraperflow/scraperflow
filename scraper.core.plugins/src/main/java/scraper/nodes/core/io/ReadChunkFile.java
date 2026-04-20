package scraper.nodes.core.io;

import scraper.annotations.*;
import scraper.api.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Reads an input file in chunks.
 * Splits after <var>splitAfterLines</var> many lines or after <var>splitAFterCharacters</var> characters read.
 */
@NodePlugin("0.2.0")
@Io
public final class ReadChunkFile implements StreamNode {

    /** Input file path */
    @FlowKey(mandatory = true) @EnsureFile
    private final T<String> inputFile = new T<>(){};

    /** Where the output line will be put */
    @FlowKey(defaultValue = "\"output\"")
    private final L<String> output = new L<>(){};

    /** Charset of the file */
    @FlowKey(defaultValue = "\"ISO_8859_1\"")
    private String charset;

    /** Split after this many lines */
    @FlowKey(defaultValue = "2300")
    private Integer splitAfterLines;

    /** Split after this many characters */
    @FlowKey(defaultValue = "130000")
    private Integer splitAfterCharacters;

    @Override
    public void process(@NotNull StreamNodeContainer n, @NotNull FlowMap o) {
        String file = o.eval(inputFile);

        try(BufferedReader reader = new BufferedReader(new FileReader(file, Charset.forName(charset)))) {
            StringBuilder splitContent = new StringBuilder();

            int currentLines = 1;
            int currentChars = 1;


            int c;
            while ((c = reader.read()) != -1) {
                splitContent.append(((char) c));
                if ((currentLines > splitAfterLines) || (currentChars > splitAfterCharacters)) {
                    FlowMap out = o.copy();
                    out.output(output, splitContent.toString());
                    n.streamFlowMap(o, out);
                    splitContent = new StringBuilder();
                    currentLines = 0;
                    currentChars = 0;
                }

                if(c == '\r' || c == '\n') { currentLines ++; }
                currentChars ++;
            }

            if (splitContent.length() > 0) {
                FlowMap out = o.copy();
                out.output(output, splitContent.toString());
                n.streamFlowMap(o, out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
