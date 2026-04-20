package scraper.nodes.core.os;

import scraper.annotations.*;
import scraper.api.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import static scraper.api.NodeLogLevel.*;

/**
 * Executes a command defined in the workflow in the current system environment.
 *
 * <ol>
 *     <li>Executes the command <var>exec</var> (or as a simple string: <var>execStr</var>)</li>
 *     <li>Waits for completion</li>
 *     <li>Logs if an error occurs. If failOnException is enabled, throws a NodeException</li>
 * </ol>
 *
 * <p>Example definition:
 *
 * <pre>
 *   type: Exec
 *   exec: ["ls", "-la", "{path}"]
 * </pre>
 */
@NodePlugin("0.6.0")
@Io
public class Exec implements FunctionalNode {

    /** Command to execute. Every element is handled as one argument. Argument strings are evaluated. */
    @FlowKey(mandatory = true)
    private final T<List<String>> exec = new T<>(){};

    /** Working directory. Defaults to the JVM working directory. */
    @FlowKey
    private final T<String> workingDirectory = new T<>(){};

    /** If the process fails, this node can throw an exception if this field is specified. */
    @FlowKey(defaultValue = "false")
    private Boolean failOnException;

    /** Byte buffer size */
    @FlowKey(defaultValue = "1000")
    private Integer ByteBuffer;

    /** Remove trailing newline */
    @FlowKey(defaultValue = "false")
    private Boolean removeTrailingNewline;

    /** Redirects process output to this string */
    @FlowKey(defaultValue = "\"_\"")
    private final L<String> put = new L<>(){};

    /** Redirects process error output to this string */
    @FlowKey(defaultValue = "\"_\"")
    private final L<String> putErr = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        List<String> exec = o.eval(this.exec);
        exec(n, exec, o);
    }

    void exec(NodeContainer<? extends Node> n, List<String> exec, FlowMap o) {
        try {
            ProcessBuilder pb = new ProcessBuilder(exec);
            o.evalMaybe(workingDirectory).ifPresent(dir -> pb.directory(new File(dir)));
            Process b = pb.start();

            String stdOut = readStream(b.getInputStream());
            String stdErr = readStream(b.getErrorStream());

            if(removeTrailingNewline && stdOut.endsWith("\n")) stdOut = stdOut.substring(0, stdOut.length()-1);

            b.waitFor();
            if(b.exitValue() != 0) n.log(ERROR,"{0} finished with non-zero exit code: {1}", exec, b.exitValue());

            o.output(put   , stdOut);
            o.output(putErr, stdErr);

            b.destroy();
            b.waitFor();
        } catch (IOException | InterruptedException e) {
            n.log(ERROR,"{0} could not be executed:", exec, e.getMessage());
            if (failOnException) throw new NodeIOException(e, "Internal process execution error");
        }
    }

    private String readStream(InputStream inputStream) throws IOException {
        final InputStream in = new BufferedInputStream(inputStream);
        final byte[] buf = new byte[ByteBuffer];

        List<Byte> bytes = new LinkedList<>();

        int bytesRead = in.read(buf);
        while(bytesRead != -1) {

            for (int i = 0; i < bytesRead; i++) {
                bytes.add(buf[i]);
            }

            bytesRead = in.read(buf);
        }

        in.close();

        byte[] bar = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) { bar[i] = bytes.get(i); }
        return new String(bar, StandardCharsets.UTF_8);
    }
}