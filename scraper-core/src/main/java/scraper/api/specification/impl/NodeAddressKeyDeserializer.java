package scraper.api.specification.impl;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import scraper.api.node.NodeAddress;
import scraper.api.node.impl.NodeAddressImpl;

public class NodeAddressKeyDeserializer extends KeyDeserializer {
 
  @Override
  public NodeAddress deserializeKey(
    String key, 
    DeserializationContext ctxt) {
      return new NodeAddressImpl(key);
    }
}