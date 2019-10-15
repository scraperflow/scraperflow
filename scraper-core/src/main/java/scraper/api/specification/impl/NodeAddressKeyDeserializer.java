package scraper.api.specification.impl;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import scraper.api.node.Address;
import scraper.api.node.impl.NodeAddress;

public class NodeAddressKeyDeserializer extends KeyDeserializer {
 
  @Override
  public Address deserializeKey(
    String key, 
    DeserializationContext ctxt) {
      return new NodeAddress(key);
    }
}