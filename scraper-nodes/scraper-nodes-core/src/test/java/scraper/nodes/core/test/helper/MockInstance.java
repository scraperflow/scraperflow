package scraper.nodes.core.test.helper;

import scraper.annotations.NotNull;
import scraper.api.node.*;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.plugin.NodeHook;
import scraper.api.reflect.T;
import scraper.api.service.ExecutorsService;
import scraper.api.service.FileService;
import scraper.api.service.HttpService;
import scraper.api.service.ProxyReservation;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class MockInstance implements ScrapeInstance {
    private NodeContainer<? extends Node> node;
    private Map<String, Object> specs;

    public MockInstance(NodeContainer<? extends Node> node, Map<String, Object> specs) {
        this.node = node;
        this.specs = specs;
    }

    @NotNull @Override public String getName() { return "mock"; }
    @NotNull @Override public Map<InstanceAddress, ScrapeInstance> getImportedInstances() { throw new IllegalStateException(); }
    @Override public Collection<NodeHook> getHooks() { throw new IllegalStateException(); }
    @Override public void init() { }
    @NotNull @Override public ScrapeSpecification getSpecification() { throw new IllegalStateException(); }
    @NotNull @Override public ExecutorsService getExecutors() { throw new IllegalStateException("Functional node called service"); }
    @NotNull @Override public HttpService getHttpService() { throw new IllegalStateException("Functional node called service"); }
    @NotNull @Override public ProxyReservation getProxyReservation() { throw new IllegalStateException("Functional node called service"); }
    @NotNull @Override public FileService getFileService() { throw new IllegalStateException("Functional node called service"); }
    @Override public void setEntry(@NotNull GraphAddress address, @NotNull NodeContainer<? extends Node> nn) { }
    @NotNull @Override public Optional<NodeContainer<? extends Node>> getEntry() { return Optional.empty(); }
    @NotNull @Override public NodeContainer<? extends Node> getNode(@NotNull NodeAddress target) { return null; }
    @NotNull @Override public Optional<NodeContainer<? extends Node>> getNode(@NotNull Address target) { return Optional.empty(); }
    @Override public Optional<NodeContainer<? extends Node>> getNode(String targetRepresentation) { return Optional.empty(); }
    @Override public void addRoute(@NotNull Address address, @NotNull NodeContainer<? extends Node> node) { }
    @NotNull @Override public Map<String, Object> getEntryArguments() { return null; }
    @Override public Map<Address, NodeContainer<? extends Node>> getRoutes() { return null; }
    @Override public <A> A evalIdentity(T<A> template) { return null; }
}
