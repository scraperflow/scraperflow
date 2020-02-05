package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.reflect.T;
import scraper.core.AbstractNode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Reads a JSON string, converts it to a JSON object. Cleans the object and returns a cleaned JSON string.
 *
 * <p>Example .scrape definition:
 *
 * <pre>
 * {
 *   "type" : "CleanJsonNode",
 *   "__comment" : "Removes personal and image related metadata. Removes for every tag the url, count, and id",
 *   "content" : "{body}",
 *   "clean" : [
 *     "num_favorites", "images",
 *     "[]tags|url",
 *     "[]tags|count",
 *     "[]tags|id"
 *   ],
 *   "put" : "body",
 * }
 * </pre>
 * </p>
 *
 * @see AbstractNode
 * @since 0.1
 * @author Albert Schimpf
 */
// TODO api doc; | descends into maps, [] descends into arrays
@NodePlugin("0.1.0")
public final class CleanJsonObjectNode implements FunctionalNode {

    /** JSON object */
    @FlowKey(mandatory = true)
    private final T<Map<String, Object>> jsonObject = new T<>(){};

    /** Clean operations */
    @FlowKey(mandatory = true)
    private List<String> clean;

    /** cleaned JSON object location */
    @FlowKey(defaultValue = "output", output = true)
    private final T<Map<String, Object>> cleanedObject = new T<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) throws NodeException {
        Map<String, Object> json = o.eval(jsonObject);

        for (String parts : clean) {
            try {
                cleanObject(json, parts.split("\\|"));
            } catch (Exception e) {
                throw new NodeException(e, "Could not execute clean operation: " + e.getLocalizedMessage());
            }
        }

        // put cleaned object
        o.output(cleanedObject, json);
    }

    private void cleanObject(Map obj, String[] parts) {
        for (int i = 0; i < parts.length; i++) {
            String node = parts[i];
            if(node.startsWith("[]")) {
                List array = (List) obj.get(node.substring(2));
                String[] subParts = Arrays.copyOfRange(parts, i+1, parts.length);

                cleanArray(array, subParts);
                return;
            } else {
                if(i == parts.length -1) {
                    obj.remove(node);
                } else {
                    obj = (Map) obj.get(node);
                }
            }
        }
    }

    private void cleanArray(List array, String[] subParts) {
        for (Object o : array) {
            String node = subParts[0];
            if(node.equals("[]")) {
                List nextArray = (List) o;
                String[] nextSubParts = Arrays.copyOfRange(subParts, 1, subParts.length);

                cleanArray(nextArray, nextSubParts);
                return;
            } else {
                cleanObject((Map) o, subParts);
            }
        }
    }

}
