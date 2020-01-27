package scraper.nodes.core.os;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;
import scraper.core.Template;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static scraper.core.NodeLogLevel.DEBUG;
import static scraper.core.NodeLogLevel.ERROR;

/**
 * Executes a command defined in the .scrape file. Parameters can be templates.
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
public final class ExecNode extends AbstractFunctionalNode {

    /** Command to execute. Every element is handled as one argument. Argument strings are evaluated. */
    @FlowKey
    private final Template<List<String>> exec = new Template<>(){};

    @FlowKey
    private final Template<String> execStr = new Template<>(){};

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
    public void modify(@NotNull final FlowMap o) throws NodeException {
        List<String> exec = this.exec.eval(o);
        String execStr = this.execStr.eval(o);

        if(exec != null) exec(exec, o);
        else if(execStr != null) exec(Arrays.asList(execStr.split("\\s")), o);
    }

    private void exec(List<String> exec, FlowMap o) throws NodeException {
        try {
            log(DEBUG,"Executing {}", exec);

            ProcessBuilder pb = new ProcessBuilder(exec);
            Process b = pb.start();
            b.waitFor();
            if(b.exitValue() != 0) log(ERROR,"{} finished with non-zero exit code: {}", exec, b.exitValue());

            String stdOut = readStream(b.getInputStream());
            String stdErr = readStream(b.getErrorStream());
            if(put    != null) o.put(put   , stdOut);
            if(putErr != null) o.put(putErr, stdErr);

            b.destroy();
            b.waitFor();
        } catch (IOException | InterruptedException e) {
            log(ERROR,"'{}' could not be executed:", exec, e.getMessage());
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