package scraper.nodes.core.os;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.node.type.Node;
import scraper.api.reflect.T;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static scraper.api.node.container.NodeLogLevel.DEBUG;
import static scraper.api.node.container.NodeLogLevel.ERROR;

/**
 * Executes a command defined in the .scrape file. Parameters can be Ts.
 *
 * <ol>
 *     <li>Executes the command <tt>exec</tt></li>
 *     <li>Waits for completion</li>
 *     <li>Logs if an error occurs. If {@code failOnException} is enabled, throws a {@code NodeException}</li>
 * </ol>
 *
 * <p>Example .scrape definition:
 *
 * <pre>
 * {
 *   "type":"ExecNode",
 *   "__comment":"Executes the 'ls -la' command and stops forwarding.",
 *   "label":"lsCommand",
 *   "exec":[
 *     "ls", "-la", "{path}"
 *   ],
 *   "forward":false
 * }
 * </pre>
 */
@NodePlugin("0.2.0")
public final class ExecNode implements FunctionalNode {

    /** Command to execute. Every element is handled as one argument. Argument strings are evaluated. */
    @FlowKey
    private final T<List<String>> exec = new T<>(){};

    @FlowKey
    private final T<String> execStr = new T<>(){};

    /** If the process fails, this node can throw an exception if this field is specified. */
    @FlowKey(defaultValue = "false")
    private Boolean failOnException;

    /** Redirects process output to this string */
    @FlowKey
    private String put;

    /** Redirects process error output to this string */
    @FlowKey
    private String putErr;

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) throws NodeException {
        Optional<List<String>> exec = o.evalMaybe(this.exec);
        Optional<String> execStr = o.evalMaybe(this.execStr);

        if(exec.isPresent()) exec(n, exec.get(), o);
        else if(execStr.isPresent()) exec(n, Arrays.asList(execStr.get().split("\\s")), o);
    }

    private void exec(NodeContainer<? extends Node> n, List<String> exec, FlowMap o) throws NodeException {
        try {
            n.log(DEBUG,"Executing {}", exec);

            ProcessBuilder pb = new ProcessBuilder(exec);
            Process b = pb.start();
            b.waitFor();
            if(b.exitValue() != 0) n.log(ERROR,"{} finished with non-zero exit code: {}", exec, b.exitValue());

            String stdOut = readStream(b.getInputStream());
            String stdErr = readStream(b.getErrorStream());
            if(put    != null) o.put(put   , stdOut);
            if(putErr != null) o.put(putErr, stdErr);

            b.destroy();
            b.waitFor();
        } catch (IOException | InterruptedException e) {
            n.log(ERROR,"'{}' could not be executed:", exec, e.getMessage());
            if (failOnException) throw new NodeException(e, "Internal process execution error");
        }
    }

    private String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ( (line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }
        String result = builder.toString();

        reader.close();

        return result;
    }
}