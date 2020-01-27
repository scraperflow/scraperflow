package scraper.nodes.core.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;
import scraper.core.Template;

import java.io.IOException;
import java.util.Map;

import static scraper.core.NodeLogLevel.WARN;

/**
 * Converts and evaluates a json string to a json object
 */
@NodePlugin("1.0.0")
public final class JsonStringToObjectNode extends AbstractFunctionalNode {

    /** JSON string */
    @FlowKey(defaultValue = "\"json\"")
    private final Template<String> jsonString = new Template<>(){};

    /** JSON object output */
    @FlowKey(defaultValue = "\"result\"", output = true)
    private final Template<Map> jsonObject = new Template<>(){};

    // used to convert JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void modify(@NotNull final FlowMap o) throws NodeException {
        String json = this.jsonString.eval(o);

        try {
            // read and clean object at argument location
            Map obj = objectMapper.readValue(json, Map.class);
            jsonObject.output(o, obj);
        }
        catch (IOException e) {
            log(WARN,"Could not convert input to JSON object: {}", json);
            throw new NodeException(e.getMessage()+"; Failed to convert JSON object: "+ json);
        }
    }
}
