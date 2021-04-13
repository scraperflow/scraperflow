package scraper.nodes.core.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeIOException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.io.IOException;

/**
 * Converts a Json object to a String representation
 */
@NodePlugin("0.3.0")
public final class ObjectToJsonString implements FunctionalNode {

    /** Json object */
    @FlowKey(defaultValue = "\"object\"")
    private final T<Object> object = new T<>(){};

    /** Resulting string location */
    @FlowKey(defaultValue = "\"result\"")
    private final L<String> result = new L<>(){};

    // used to convert JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        Object object = o.eval(this.object);

        try {
            // read object at argument location
            String obj = objectMapper.writeValueAsString(object);
            o.output(result, obj);
        }
        catch (IOException e) {
            throw new NodeIOException(e, e.getMessage()+"; Failed to convert JSON object");
        }
    }
}
