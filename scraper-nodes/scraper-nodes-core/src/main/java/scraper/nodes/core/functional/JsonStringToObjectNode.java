package scraper.nodes.core.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.io.IOException;
import java.util.Map;

import static scraper.api.node.container.NodeLogLevel.WARN;

/**
 * Converts and evaluates a json string to a json object
 */
@NodePlugin("0.1.0")
public final class JsonStringToObjectNode implements FunctionalNode {

    /** JSON string */
    @FlowKey(defaultValue = "\"json\"")
    private final T<String> jsonString = new T<>(){};

    /** JSON object output */
    @FlowKey(defaultValue = "\"result\"")
    private final L<Map<?, ?>> jsonObject = new L<>(){};

    // used to convert JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) throws NodeException {
        String json = o.eval(jsonString);

        try {
            // read and clean object at argument location
            Map<?, ?> obj = objectMapper.readValue(json, Map.class);
            o.output(jsonObject, obj);
        }
        catch (IOException e) {
            n.log(WARN,"Could not convert input to JSON object: {}", json);
            throw new NodeException(e, e.getMessage()+"; Failed to convert JSON object: "+ json);
        }
    }
}
