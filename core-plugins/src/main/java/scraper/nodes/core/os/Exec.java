package scraper.nodes.core.os;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.Io;
import scraper.annotations.NodePlugin;
import scraper.api.NodeIOException;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.NodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.Node;
import scraper.api.L;
import scraper.api.T;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
 *   type: ExecNode
 *   exec: ["ls", "-la", "{path}"]
 * </pre>
 */
@NodePlugin("0.5.0")
@Io
public final class Exec implements FunctionalNode {

    /** Command to execute. Every element is handled as one argument. Argument strings are evaluated. */
    @FlowKey
    private final T<List<String>> exec = new T<>(){};

    /** Single String alternative to <var>exec</var>, will get executed if <var>exec</var> is not specified */
    @FlowKey
    private final T<String> execStr = new T<>(){};

    /** Working directory. Defaults to the JVM working directory. */
    @FlowKey
    private final T<String> workingDirectory = new T<>(){};

    /** If the process fails, this node can throw an exception if this field is specified. */
    @FlowKey(defaultValue = "false")
    private Boolean failOnException;

    /** Redirects process output to this string */
    @FlowKey(defaultValue = "\"_\"")
    private final L<String> put = new L<>(){};

    /** Redirects process error output to this string */
    @FlowKey(defaultValue = "\"_\"")
    private final L<String> putErr = new L<>(){};

    /** Byte buffer size */
    @FlowKey(defaultValue = "1000")
    private Integer ByteBuffer;


    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        Optional<List<String>> exec = o.evalMaybe(this.exec);
        Optional<String> execStr = o.evalMaybe(this.execStr);

        if(exec.isPresent()) exec(n, exec.get(), o);
        else execStr.ifPresent(s -> exec(n, Arrays.asList(s.split("\\s")), o));
    }

    private void exec(NodeContainer<? extends Node> n, List<String> exec, FlowMap o) {
        try {

            ProcessBuilder pb = new ProcessBuilder(exec);
            o.evalMaybe(workingDirectory).ifPresent(dir -> pb.directory(new File(dir)));
            Process b = pb.start();

            String stdOut = readStream(b.getInputStream());
            String stdErr = readStream(b.getErrorStream());

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

        StringBuilder builder = new StringBuilder();

        int bytesRead = in.read(buf);
        while(bytesRead != -1) {

            for (int i = 0; i < bytesRead; i++) {
                builder.append((char) buf[i]);
            }

            bytesRead = in.read(buf);
        }

        in.close();

        return builder.toString();
    }
}