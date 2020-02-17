package scraper.nodes.core.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.reflect.T;

import java.io.IOException;

/**
 * Converts a Json object (map) to a String representation
 */
@NodePlugin("0.1.0")
public final class ObjectToJsonStringNode implements FunctionalNode {

    /** Json object */
    @FlowKey(defaultValue = "\"object\"")
    private final T<Object> object = new T<>(){};

    /** Resulting string location */
    @FlowKey(defaultValue = "\"result\"", output = true)
    private final T<String> result = new T<>(){};

    // used to convert JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) throws NodeException {
        Object object = o.eval(this.object);

        try {
            // read object at argument location
            String obj = objectMapper.writeValueAsString(object);
            o.output(result, obj);
        }
        catch (IOException e) {
            throw new NodeException(e, e.getMessage()+"; Failed to convert JSON object");
        }
    }
}
