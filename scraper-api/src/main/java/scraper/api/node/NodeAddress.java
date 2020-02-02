package scraper.api.node;

import scraper.api.specification.ScrapeInstance;

import java.util.Optional;

public interface NodeAddress extends Address {
    Address nextIndex();
    Address replace(String representation);
//    Address getLabelRepresentation();
}
