package scraper.addons;

import scraper.annotations.NotNull;
import scraper.api.node.Node;
import scraper.api.node.Address;
import scraper.api.service.ExecutorsService;
import scraper.api.service.FileService;
import scraper.api.service.HttpService;
import scraper.api.service.ProxyReservation;
import scraper.api.specification.ScrapeInstance;
import scraper.core.AbstractNode;

import java.util.List;
import java.util.Map;

@SuppressWarnings("ConstantConditions") //mock
public abstract class TestNode extends AbstractNode {
    public TestNode() {
        // TODO mocking framework
        this.jobPojo = new ScrapeInstance() {
            @NotNull @Override public Map<String, Object> getInitialArguments() { return null; }
            @NotNull @Override public Map<String, Map<String, Object>> getGlobalNodeConfigurations() { return null; }
            @NotNull
            @Override public String getName() { return null; }
            @NotNull @Override public Node getNode(@NotNull Address target) { return null; }
            @Override public Address getForwardTarget(@NotNull Address origin) { return null; }
            @NotNull @Override public Map<Address, List<Node>> getGraphs() { return null; }
            @NotNull @Override public List<Node> getEntryGraph() { return null; }
            @NotNull @Override public List<Node> getGraph(@NotNull Address graph) { return null; }
            @NotNull
            @Override public ExecutorsService getExecutors() { return null; }
            @NotNull
            @Override public HttpService getHttpService() { return null; }
            @NotNull
            @Override public ProxyReservation getProxyReservation() { return null; }
            @NotNull
            @Override public FileService getFileService() { return null; }
        };
    }
}
