package scraper.nodes.core.test.helper;

import scraper.annotations.NotNull;
import scraper.api.node.*;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.service.ExecutorsService;
import scraper.api.service.FileService;
import scraper.api.service.HttpService;
import scraper.api.service.ProxyReservation;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MockInstance implements ScrapeInstance {
    private NodeContainer<? extends Node> node;
    private Map<String, Object> specs;

    public MockInstance(NodeContainer<? extends Node> node, Map<String, Object> specs) {
        this.node = node;
        this.specs = specs;
    }

//    @NotNull @Override public Map<String, Object> getInitialArguments() { return Map.of(); }
//    @NotNull @Override public Map<String, Map<String, Object>> getGlobalNodeConfigurations() { return Map.of("0", specs); }
    @NotNull @Override public String getName() { return "mock"; }
//    @NotNull @Override public NodeContainer<? extends Node> getNode(@NotNull Address target) { return node; }
//    @NotNull @Override public Map<GraphAddress, List<NodeContainer<? extends Node>>> getGraphs() { throw new IllegalStateException(); }
//    @NotNull @Override public List<NodeContainer<? extends Node>> getEntryGraph() { return List.of(node); }
//    @NotNull @Override public List<NodeContainer<? extends Node>> getGraph(@NotNull GraphAddress label) { return getEntryGraph(); }
    @NotNull @Override public Map<InstanceAddress, ScrapeInstance> getImportedInstances() { throw new IllegalStateException(); }

    @Override
    public Collection<NodeHook> getBeforeHooks() {
        throw new IllegalStateException();
    }

    @Override
    public Collection<NodeHook> getAfterHooks() {
        throw new IllegalStateException();
    }

    @Override public void init() { }

    @NotNull
    @Override
    public ScrapeSpecification getSpecification() {
        throw new IllegalStateException();
    }

    //    @Override public Address getForwardTarget(@NotNull NodeAddress origin) { throw new IllegalStateException(); }
    @NotNull @Override public ExecutorsService getExecutors() { throw new IllegalStateException("Functional node called service"); }
    @NotNull @Override public HttpService getHttpService() { throw new IllegalStateException("Functional node called service"); }
    @NotNull @Override public ProxyReservation getProxyReservation() { throw new IllegalStateException("Functional node called service"); }
    @NotNull @Override public FileService getFileService() { throw new IllegalStateException("Functional node called service"); }

    @Override
    public void setEntry(@NotNull GraphAddress address, @NotNull NodeContainer<? extends Node> nn) {

    }

    @NotNull
    @Override
    public NodeContainer<? extends Node> getEntry() {
        return null;
    }

    @NotNull
    @Override
    public NodeContainer<? extends Node> getNode(@NotNull NodeAddress target) {
        return null;
    }

    @NotNull
    @Override
    public Optional<NodeContainer<? extends Node>> getNode(@NotNull Address target) {
        return Optional.empty();
    }

    @Override
    public void addRoute(@NotNull Address address, @NotNull NodeContainer<? extends Node> node) {

    }

    @NotNull
    @Override
    public Map<String, Object> getEntryArguments() {
        return null;
    }

}
