package scraper.core;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.annotations.node.NodePlugin;
import scraper.api.node.type.Node;


/**
 * Basic abstract implementation of a Node with labeling and goTo support.
 * <p>
 * Provides following utility functions:
 * <ul>
 *     <li>Node factory method depending on the defined type</li>
 *     <li>Node coordination</li>
 *     <li>Argument evaluation</li>
 *     <li>Key reservation</li>
 *     <li>Ensure file</li>
 *     <li>Basic ControlFlow implementation</li>
 *     <li>Thread service pool management</li>
 * </ul>
 * </p>
 */
@SuppressWarnings({"WeakerAccess", "unused"}) // abstract implementation
@NodePlugin("0.1.0")
public abstract class GenericNode extends AbstractNode<Node> {
    public GenericNode(@NotNull String instance, @NotNull String graph, @Nullable String node, int index) {
        super(instance, graph, node, index);
    }
}
