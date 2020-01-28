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

/**
 * Converts a Json object (map) to a String representation
 */
@NodePlugin("1.0.0")
public final class ObjectToJsonStringNode extends AbstractFunctionalNode {

    /** Json object */
    @FlowKey(defaultValue = "\"object\"")
    private final Template<Object> object = new Template<>(){};

    /** Resulting string location */
    @FlowKey(defaultValue = "\"result\"", output = true)
    private final Template<String> result = new Template<>(){};

    // used to convert JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void modify(@NotNull final FlowMap o) throws NodeException {
        Object object = this.object.eval(o);

        try {
            // read object at argument location
            String obj = objectMapper.writeValueAsString(object);
            result.output(o, obj);
        }
        catch (IOException e) {
            throw new NodeException(e, e.getMessage()+"; Failed to convert JSON object");
        }
    }
}
