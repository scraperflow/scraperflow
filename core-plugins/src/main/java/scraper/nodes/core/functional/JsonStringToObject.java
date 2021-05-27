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

import static scraper.api.node.container.NodeLogLevel.ERROR;

/**
 * Converts and evaluates a json string to a json object
 */
@NodePlugin(value = "0.3.0")
public final class JsonStringToObject implements FunctionalNode {

    /** JSON string */
    @FlowKey(mandatory = true)
    private final T<String> jsonString = new T<>(){};

    /** JSON object output */
    @FlowKey(mandatory = true)
    private final L<Object> jsonObject = new L<>(){};

    // used to convert JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) throws NodeIOException {
        String json = o.eval(jsonString);

        try {
            // read and clean object at argument location
            Object obj = objectMapper.readValue(json, Object.class);
            o.output(jsonObject, obj);
        }
        catch (IOException e) {
            String err = String.format("Could not convert input to JSON object: %s\n%s", json, e.getMessage());
            n.log(ERROR,err);
            throw new NodeIOException(e, err);
        }
    }
}
