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
 * @since 1.0.0
 */
public interface ControlFlow {
    /** @return control flow to output nodes */
    @NotNull List<ControlFlowEdge> getOutput();

    /** @return control flow from input nodes */
    @NotNull List<ControlFlowEdge> getInput();

    /** @return formatted display name of this node */
    @NotNull String getDisplayName();
}
