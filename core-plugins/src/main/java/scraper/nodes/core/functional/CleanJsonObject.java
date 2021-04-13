package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeIOException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Reads a JSON map object and cleans the object and returns a cleaned JSON map.
 *
 * <p>Example  definition:
 *
 * <pre>
 *type: CleanJson
 *jsonObject: "{body}"
 *clean: # remove fields 'num_favorites', 'images', and in the 'tags' array the fields 'url', 'count', and 'id'
 *  - "num_favorites"
 *  - "images"
 *  - "[]tags|url"
 *  - "[]tags|count"
 *  - "[]tags|id"
 *cleanedObject: cleanBody
 * </pre>
 */
@NodePlugin("0.2.0")
public final class CleanJsonObject <A> implements FunctionalNode {

    /** JSON map object to clean */
    @FlowKey(mandatory = true)
    private final T<Map<String, A>> jsonObject = new T<>(){};

    /** Clean operations where <code>|</code> descends into maps and <code>[]</code> descends into arrays */
    @FlowKey(mandatory = true)
    private final T<List<String>> clean = new T<>(){};

    /** Where the cleaned JSON object will be stored */
    @FlowKey(defaultValue = "\"output\"")
    private final L<Map<String, A>> cleanedObject = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        Map<String, A> json = o.eval(jsonObject);
        List<String> clean = o.evalIdentity(this.clean);

        for (String parts : clean) {
            try {
                cleanObject(json, parts.split("\\|"));
            } catch (Exception e) {
                throw new NodeIOException(e, "Could not execute clean operation: " + e.getLocalizedMessage());
            }
        }

        // put cleaned object
        o.output(cleanedObject, json);
    }

    // types do not matter
    @SuppressWarnings("rawtypes")
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

    // types do not matter
    @SuppressWarnings("rawtypes")
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
