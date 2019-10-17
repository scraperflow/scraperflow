package scraper.api.specification.impl;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import scraper.api.node.Address;
import scraper.api.node.impl.AddressImpl;

public class AddressKeyDeserializer extends KeyDeserializer {
 
  @Override
  public Address deserializeKey(
    String key, 
    DeserializationContext ctxt) {
      return new AddressImpl(key);
    }
}