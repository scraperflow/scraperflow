package scraper.nodes.core.io;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.reflect.T;
import scraper.core.AbstractNode;

import java.io.File;

/**
 * Checks if file exists
 *
 * @see AbstractNode
 * @author Albert Schimpf
 */
@NodePlugin("0.1.0")
public final class FileExistsNode implements FunctionalNode {

    /** Path of the file to be cleared */
    @FlowKey(mandatory = true) @NotNull
    private final T<String> path = new T<>(){};

    @FlowKey(defaultValue = "\"exists\"", output = true) @NotNull
    private final T<Boolean> result = new T<>(){};

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
