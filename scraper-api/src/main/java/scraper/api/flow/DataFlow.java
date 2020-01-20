package scraper.api.flow;

import scraper.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Describes the data flow for a single node.
 * The data flow consists of:
 * <ol>
 *     <li>Input flow</li>
 *     <li>Output flow</li>
 * </ol>
 *
 * @since 1.0.0
 */
public interface DataFlow {
    /** @return input data flow, key -> type */
    @NotNull Map<String, String> getInputData();

    /** @return output data flow, key -> type */
    @NotNull Map<String, String> getOutputData();


//    /** @return formatted display name of this node */
//    @NotNull String getDisplayName();
}
