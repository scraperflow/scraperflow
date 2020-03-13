package scraper.nodes.core.io;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.io.File;

/**
 * Checks if file exists
 */
@NodePlugin("0.1.0")
public final class FileExistsNode implements FunctionalNode {

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
