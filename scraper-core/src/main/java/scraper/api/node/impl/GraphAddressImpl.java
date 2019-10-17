package scraper.api.node.impl;

import scraper.annotations.NotNull;
import scraper.api.node.GraphAddress;

public class GraphAddressImpl extends AddressImpl implements GraphAddress {
    public GraphAddressImpl(@NotNull String label) {
        super(label);
    }
}
