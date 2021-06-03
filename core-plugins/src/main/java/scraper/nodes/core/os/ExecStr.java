package scraper.nodes.core.os;

import scraper.annotations.FlowKey;
import scraper.annotations.Io;
import scraper.annotations.NodePlugin;
import scraper.annotations.NotNull;
import scraper.api.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static scraper.api.NodeLogLevel.ERROR;

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
 *   type: ExecStr
 *   exec: "ls -la {path}"
 * </pre>
 */
@NodePlugin("0.5.0")
@Io
public final class ExecStr extends Exec {

    /** Single String alternative to <var>exec</var>, will get executed if <var>exec</var> is not specified */
    @FlowKey(mandatory = true)
    private final T<String> exec = new T<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        String exec = o.eval(this.exec);
        exec(n, Arrays.asList(exec.split("\\s")), o);
    }
}