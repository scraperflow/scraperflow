package scraper.addons;

import scraper.api.node.Node;
import scraper.api.node.NodeAddress;
import scraper.api.service.ExecutorsService;
import scraper.api.service.FileService;
import scraper.api.service.HttpService;
import scraper.api.service.ProxyReservation;
import scraper.api.specification.ScrapeInstance;
import scraper.core.AbstractNode;

import java.util.List;
import java.util.Map;

public abstract class TestNode extends AbstractNode {
    public TestNode() {
        // TODO mocking framework
        this.jobPojo = new ScrapeInstance() {
            @Override public Map<String, Object> getInitialArguments() { return null; }
            @Override public Map<String, Map<String, Object>> getGlobalNodeConfigurations() { return null; }
            @Override public String getName() { return null; }
            @Override public String getDescription() { return null; }
            @Override public Node getNode(NodeAddress target) { return null; }
            @Override public NodeAddress getForwardTarget(NodeAddress origin) { return null; }
            @Override public List<Node> getMainFlow() { return null; }
            @Override public List<List<Node>> getFragmentFlows() { return null; }
            @Override public ExecutorsService getExecutors() { return null; }
            @Override public HttpService getHttpService() { return null; }
            @Override public ProxyReservation getProxyReservation() { return null; }
            @Override public FileService getFileService() { return null; }
        };
    }
}
