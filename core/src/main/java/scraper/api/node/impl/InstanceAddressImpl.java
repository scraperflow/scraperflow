package scraper.api.node.impl;

import scraper.annotations.NotNull;
import scraper.api.node.Address;
import scraper.api.node.InstanceAddress;
import scraper.util.NodeUtil;

public class InstanceAddressImpl extends AddressImpl implements InstanceAddress {
    public InstanceAddressImpl(@NotNull String label) {
        super(label);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if(o instanceof Address) {
            Address that = (Address) o;
            return NodeUtil.representationEquals(getRepresentation(), that.getRepresentation());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return NodeUtil.representationHashCode(getRepresentation());
    }
}
