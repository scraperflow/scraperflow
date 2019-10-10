package scraper.api.flow;

import scraper.annotations.NotNull;

import java.util.List;

/**
 * Describes the control flow for a single node.
 * The control flow consists of:
 * <ol>
 *     <li>Input flow</li>
 *     <li>Output flow</li>
 * </ol>
 *
 * The interface also provides a method to generate a display name for the current node.
 *
 * @since 1.0.0
 */
public interface ControlFlow {
    /** @return control flow to output nodes */
    @NotNull List<ControlFlowEdge> getOutput();

    /** @return control flow from input nodes */
    @NotNull List<ControlFlowEdge> getInput();

    /** @return display name of this node */
    @NotNull String getDisplayName();

    /** @return fragment of this node; null if none */
    // FIXME necessary?
    @NotNull String getFragment();
}
