package scraper.plugins.core.flowgraph.impl;


import scraper.annotations.NotNull;
import scraper.api.Address;
import scraper.api.NodeContainer;
import scraper.plugins.core.flowgraph.api.ControlFlowNode;

/**
 * @since 1.0.0
 */
public class ControlFlowNodeImpl implements ControlFlowNode {

    Address address;
    String type;

    public ControlFlowNodeImpl(NodeContainer<?> node) {
        this.address = node.getAddress();
        this.type = node.getC().getClass().getSimpleName();
    }

    @NotNull
    @Override public Address getAddress() {
        return address;
    }
    public void setAddress(Address address) { this.address = address; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}

