package scraper.nodes.core.stream;

import scraper.annotations.FlowKey;
import scraper.annotations.Io;
import scraper.annotations.NodePlugin;
import scraper.api.NodeIOException;
import scraper.api.FlowMap;
import scraper.api.StreamNodeContainer;
import scraper.api.StreamNode;
import scraper.api.L;
import scraper.api.T;

import java.io.*;
import java.util.List;
import java.util.Random;

import static scraper.api.NodeLogLevel.DEBUG;
import static scraper.api.NodeLogLevel.ERROR;

/**
 * Executes a command defined in the workflow in the current system environment.
 * In contrast to 'Exec', the output of the command is treated as a stream (per line).
 */
@NodePlugin("0.0.2")
@Io
public final class ExecStream implements StreamNode {

    /** Command to execute. Every element is handled as one argument. Argument strings are evaluated. */
    @FlowKey(mandatory = true)
    private final T<List<String>> exec = new T<>(){};

    /** Working directory. Defaults to the JVM working directory. */
    @FlowKey(mandatory = true)
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

    @Override
    public void process(StreamNodeContainer n, FlowMap o) {
        String intstr = String.valueOf(new Random().nextInt(560000000));

        List<String> exec = o.eval(this.exec);

        try {
            n.log(DEBUG,"Executing {0}", exec);

            ProcessBuilder pb = new ProcessBuilder(exec);
            o.evalMaybe(workingDirectory).ifPresent(dir -> pb.directory(new File(dir)));
            Process b = pb.start();


            StreamGobbler stdIn = new StreamGobbler(b.getInputStream(), o, n, put, putErr);
            StreamGobbler stdOut = new StreamGobbler(b.getErrorStream(), o, n, putErr, put);

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
            if (failOnException) throw new NodeIOException(e, "Internal process execution error");
        }
    }


    private static class StreamGobbler extends Thread {
        L<String> empty;
        InputStream is;
        FlowMap o;
        StreamNodeContainer c;
        L<String> loc;

        private StreamGobbler(InputStream is, FlowMap o, StreamNodeContainer c, L<String> loc, L<String> empty) {
            this.is = is;
            this.o = o;
            this.c = c;
            this.loc = loc;
            this.empty = empty;
        }

        @Override
        public void run() {
            try ( InputStreamReader isr = new InputStreamReader(is); BufferedReader br = new BufferedReader(isr) ) {
                String line;
                while ((line = br.readLine()) != null) {
                    FlowMap copy = o.copy();
                    copy.output(this.loc, line);
                    copy.output(this.empty, "");
                    c.streamFlowMap(o, copy);
                }
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}