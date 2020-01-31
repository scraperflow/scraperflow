package scraper.api.specification.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import scraper.api.node.Address;
import scraper.api.node.impl.AddressImpl;

import java.io.IOException;
import java.math.BigDecimal;

public class AddressDeserializer extends StdDeserializer<Address> {
 
    public AddressDeserializer() {
        this(null); 
    } 
 
    public AddressDeserializer(Class<?> vc) {
        super(vc); 
    }
 
    @Override
    public Address deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        if ((node.getNodeType().compareTo(JsonNodeType.STRING) == 0)) {
            return new AddressImpl(node.asText());
        } else {
            throw new IOException("Expected string, got " + node.getNodeType());
        }
    }

    @Override
    public Address getNullValue(DeserializationContext ctxt) throws JsonMappingException {

        return null;
    }
}