package scraper.api.specification.impl;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import scraper.api.node.Address;
import scraper.api.node.GraphAddress;
import scraper.api.node.impl.AddressImpl;
import scraper.api.node.impl.GraphAddressImpl;

public class GraphAddressKeyDeserializer extends KeyDeserializer {
 
  @Override
  public GraphAddress deserializeKey(
    String key, 
    DeserializationContext ctxt) {
      return new GraphAddressImpl(key);
    }
}