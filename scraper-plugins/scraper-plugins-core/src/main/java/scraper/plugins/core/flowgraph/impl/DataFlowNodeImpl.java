//package scraper.plugins.core.flowgraph.impl;
//
//
//import scraper.annotations.NotNull;
//import scraper.api.node.Address;
//import scraper.plugins.core.flowgraph.api.DataFlowNode;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @since 1.0.0
// */
//public class DataFlowNodeImpl implements DataFlowNode {
//
//    private final Address address;
//    private final Map<String, String> consumes = new HashMap<>();
//    private final Map<String, String> produces = new HashMap<>();
//
//    public DataFlowNodeImpl(@NotNull Address address) {
//        this.address = address;
//    }
//
//    @Override @NotNull public Address getAddress() { return address; }
//    @Override @NotNull public Map<String, String> consumes() { return consumes; }
//    @Override @NotNull public Map<String, String> produces() { return produces; }
//
//    public void addConsume(String key, String type) { consumes.put(key, type); }
//    public void addProduce(String key, String type) { produces.put(key, type); }
//}
//
