package scraper.api.specification.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import scraper.api.node.GraphAddress;
import scraper.api.node.impl.GraphAddressImpl;

import java.io.IOException;

public class GraphAddressDeserializer extends StdDeserializer<GraphAddress> {

    public GraphAddressDeserializer() {
        this(null);
    }

    public GraphAddressDeserializer(Class<?> vc) {
        super(vc); 
    }
 
    @Override
    public GraphAddress deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        if ((node.getNodeType().compareTo(JsonNodeType.STRING) == 0)) {
            return new GraphAddressImpl(node.asText());
        } else {
            throw new IOException("Expected string, got " + node.getNodeType());
        }
    }
}