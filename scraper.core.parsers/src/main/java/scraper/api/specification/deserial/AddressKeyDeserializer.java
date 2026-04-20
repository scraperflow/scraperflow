package scraper.api.specification.deserial;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import scraper.api.Address;
import scraper.util.NodeUtil;

public class AddressKeyDeserializer extends KeyDeserializer {
 
  @Override
  public Address deserializeKey(
    String key, 
    DeserializationContext ctxt) {
      return NodeUtil.addressOf(key);
    }
}