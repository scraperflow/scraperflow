package scraper.plugins.core.flowgraph.impl;


import scraper.annotations.NotNull;
import scraper.api.node.Address;
import scraper.plugins.core.flowgraph.api.ControlFlowNode;

/**
 * @since 1.0.0
 */
public class ControlFlowNodeImpl implements ControlFlowNode {

    public ControlFlowNodeImpl(Address address) {
        this.address = address;
    }

    Address address;

    @NotNull
    @Override
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) { this.address = address; }
}

