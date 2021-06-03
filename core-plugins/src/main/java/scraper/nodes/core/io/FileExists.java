package scraper.nodes.core.io;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.Io;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

import java.io.File;

/**
 * Checks if a file exists.
 */
@NodePlugin("0.1.0")
@Io
public final class FileExists implements FunctionalNode {

    /** Path of the file to be cleared */
    @FlowKey(mandatory = true)
    private final T<String> path = new T<>(){};

    /** Where the result of the check */
    @FlowKey(defaultValue = "\"exists\"")
    private final L<Boolean> result = new L<>(){};

    /** Treat empty files as not existing flag */
    @FlowKey(defaultValue = "true")
    private Boolean treatEmptyAsNonExisting;

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, final @NotNull FlowMap o) {
        String path = o.eval(this.path);

        File check = new File(path);

        boolean exists;

        if (check.exists()) {
            exists = !treatEmptyAsNonExisting || check.length() != 0;
        } else {
            exists = false;
        }

        o.output(result, exists);
    }
}
