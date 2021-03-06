package scraper.nodes.core.functional;

import scraper.annotations.*;
import scraper.api.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/**
 * Converts a Json object to a String representation
 */
@NodePlugin("0.4.0")
public final class ObjectToJsonString implements FunctionalNode {

    /** Json object */
    @FlowKey(mandatory = true)
    private final T<Object> object = new T<>(){};

    /** Resulting string location */
    @FlowKey(mandatory = true)
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
            n.log(NodeLogLevel.ERROR, "Failed to convert JSON object: {0}", e.getMessage());
            throw new NodeIOException(e, e.getMessage()+"; Failed to convert JSON object");
        }
    }
}
