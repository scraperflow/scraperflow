package scraper.nodes.core.stream;

import scraper.annotations.node.FlowKey;
import scraper.annotations.node.Io;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.StreamNodeContainer;
import scraper.api.node.type.StreamNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.io.*;
import java.util.List;

import static scraper.api.node.container.NodeLogLevel.DEBUG;
import static scraper.api.node.container.NodeLogLevel.ERROR;

/**
 * Executes a command defined in the workflow in the current system environment.
 */
@NodePlugin("0.0.1")
@Io
public final class ExecStreamNode implements StreamNode {

    /** Command to execute. Every element is handled as one argument. Argument strings are evaluated. */
    @FlowKey
    private final T<List<String>> exec = new T<>(){};

    /** Working directory. Defaults to the JVM working directory. */
    @FlowKey
    private final T<String> workingDirectory = new T<>(){};

    /** If the process fails, this node can throw an exception if this field is specified. */
    @FlowKey(defaultValue = "false")
    private Boolean failOnException;

    /** Redirects process output to this string */
    @FlowKey(defaultValue = "\"_\"")
    private final L<String> put = new L<>(){};

    @Override
    public void process(StreamNodeContainer n, FlowMap o) throws NodeException {
        List<String> exec = o.eval(this.exec);

        try {
            n.log(DEBUG,"Executing {0}", exec);

            ProcessBuilder pb = new ProcessBuilder(exec);
            o.evalMaybe(workingDirectory).ifPresent(dir -> pb.directory(new File(dir)));
            Process b = pb.start();


            StreamGobbler stdIn = new StreamGobbler(b.getInputStream(), o, n, put);
            StreamGobbler stdOut = new StreamGobbler(b.getErrorStream(), o, n, put);

            stdIn.start();
            stdOut.start();

            stdIn.join();
            stdOut.join();

            b.waitFor();
            if(b.exitValue() != 0) n.log(ERROR,"{0} finished with non-zero exit code: {1}", exec, b.exitValue());

            b.destroy();
            b.waitFor();
        } catch (IOException | InterruptedException e) {
            n.log(ERROR,"{0} could not be executed:", exec, e.getMessage());
            if (failOnException) throw new NodeException(e, "Internal process execution error");
        }
    }


    private static class StreamGobbler extends Thread {
        InputStream is;
        FlowMap o;
        StreamNodeContainer c;
        L<String> loc;

        private StreamGobbler(InputStream is, FlowMap o, StreamNodeContainer c, L<String> loc) {
            this.is = is;
            this.o = o;
            this.c = c;
            this.loc = loc;
        }

        @Override
        public void run() {
            try ( InputStreamReader isr = new InputStreamReader(is); BufferedReader br = new BufferedReader(isr) ) {
                String line;
                while ((line = br.readLine()) != null) {
                    c.streamElement(o, this.loc, line);
                }
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}