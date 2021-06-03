package scraper.nodes.core.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.NodeIOException;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.NodeLogLevel;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

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
