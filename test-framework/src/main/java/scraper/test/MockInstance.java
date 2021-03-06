package scraper.test;

import scraper.annotations.NotNull;
import scraper.api.*;

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

    @NotNull
    @Override public String getName() { return "mock"; }
    @NotNull
    @Override public Map<InstanceAddress, ScrapeInstance> getImportedInstances() { throw new IllegalStateException(); }
    @NotNull
    @Override public Collection<NodeHook> getHooks() { throw new IllegalStateException(); }
    @Override public void init() { }

    @Override
    public void initC() throws ValidationException { }

    @Override
    public List<ValidationException> initWithErrors() { return null; }

    @NotNull
    @Override public ScrapeSpecification getSpecification() { throw new IllegalStateException(); }
    @NotNull
    @Override public ExecutorsService getExecutors() { throw new IllegalStateException("Functional node called service"); }
    @NotNull
    @Override public HttpService getHttpService() { throw new IllegalStateException("Functional node called service"); }
    @NotNull
    @Override public ProxyReservation getProxyReservation() { throw new IllegalStateException("Functional node called service"); }
    @NotNull
    @Override public FileService getFileService() { throw new IllegalStateException("Functional node called service"); }
    @Override public void setEntry(@NotNull GraphAddress address, @NotNull NodeContainer<? extends Node> nn) { }
    @NotNull
    @Override public Optional<NodeContainer<? extends Node>> getEntry() { return Optional.empty(); }
    @NotNull
    @Override public NodeContainer<? extends Node> getNode(@NotNull NodeAddress target) { return null; }
    @NotNull
    @Override public Optional<NodeContainer<? extends Node>> getNode(@NotNull Address target) { return Optional.empty(); }
    @NotNull
    @Override public Optional<NodeContainer<? extends Node>> getNode(@NotNull String targetRepresentation) { return Optional.empty(); }
    @Override public void addRoute(@NotNull Address address, @NotNull NodeContainer<? extends Node> node) { }
    @NotNull
    @Override public Map<String, Object> getEntryArguments() { return null; }
    @NotNull
    @Override public Map<Address, NodeContainer<? extends Node>> getRoutes() { return null; }
    @NotNull
    @Override public <A> A evalIdentity(@NotNull T<A> template) { return null; }
}
