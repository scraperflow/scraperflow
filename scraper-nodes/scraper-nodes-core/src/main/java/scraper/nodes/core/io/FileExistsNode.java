package scraper.nodes.core.io;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;
import scraper.core.AbstractNode;
import scraper.core.Template;

import java.io.File;

/**
 * Checks if file exists
 *
 * @see AbstractNode
 * @since 0.1
 * @author Albert Schimpf
 */
@NodePlugin("0.1.0")
public final class FileExistsNode extends AbstractFunctionalNode {

    /** Path of the file to be cleared */
    @FlowKey(mandatory = true) @NotNull
    private final Template<String> path = new Template<>(){};

    @FlowKey(defaultValue = "\"exists\"", output = true) @NotNull
    private final Template<Boolean> result = new Template<>(){};

    @FlowKey(defaultValue = "true")
    private Boolean treatEmptyAsNonExisting;

    @Override
    public void modify(final @NotNull FlowMap o) {
        String path = this.path.eval(o);

        File check = new File(path);

        boolean exists;

        if (check.exists()) {
            exists = !treatEmptyAsNonExisting || check.length() != 0;
        } else {
            exists = false;
        }

        this.result.output(o, exists);
    }
}
