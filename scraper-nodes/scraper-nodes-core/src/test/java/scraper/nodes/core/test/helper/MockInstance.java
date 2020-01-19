package scraper.nodes.core.test.helper;

import scraper.api.exceptions.ValidationException;
import scraper.api.node.*;
import scraper.api.service.ExecutorsService;
import scraper.api.service.FileService;
import scraper.api.service.HttpService;
import scraper.api.service.ProxyReservation;
import scraper.api.specification.ScrapeInstance;
import scraper.util.NodeUtil;

import java.util.List;
import java.util.Map;

public class MockInstance implements ScrapeInstance {
    private Node node;
    private Map<String, Object> specs;

    public MockInstance(Node node, Map<String, Object> specs) {
        this.node = node;
        this.specs = specs;
    }

    @Override public Map<String, Object> getInitialArguments() { return Map.of(); }
    @Override public Map<String, Map<String, Object>> getGlobalNodeConfigurations() { return Map.of("0", specs); }
    @Override public Node getNode(Address target) { return node; }


    @Override public String getName() { return "mock"; }
    @Override public List<Node> getEntryGraph() { return List.of(node); }

    @Override public List<Node> getGraph(GraphAddress label) { return getEntryGraph(); }
    @Override public Map<InstanceAddress, ScrapeInstance> getImportedInstances() { throw new IllegalStateException(); }
    @Override public void init() { }
    @Override public Address getForwardTarget(NodeAddress origin) { throw new IllegalStateException(); }
    @Override public Map<GraphAddress, List<Node>> getGraphs() { return Map.of(NodeUtil.graphAddressOf("start"), getEntryGraph()); }

    @Override public ExecutorsService getExecutors() { throw new IllegalStateException("Functional node called service"); }
    @Override public HttpService getHttpService() { throw new IllegalStateException("Functional node called service"); }
    @Override public ProxyReservation getProxyReservation() { throw new IllegalStateException("Functional node called service"); }
    @Override public FileService getFileService() { throw new IllegalStateException("Functional node called service"); }
}
