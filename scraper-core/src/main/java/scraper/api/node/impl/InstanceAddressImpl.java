package scraper.api.node.impl;

import scraper.annotations.NotNull;
import scraper.api.node.Address;
import scraper.api.node.InstanceAddress;

public class InstanceAddressImpl extends AddressImpl implements InstanceAddress {
    public InstanceAddressImpl(@NotNull String label) {
        super(label);
    }
}
