package scraper.test;

import scraper.annotations.NotNull;
import scraper.api.NodeException;
import scraper.api.ValidationException;
import scraper.api.FlowMap;
import scraper.api.Address;
import scraper.api.GraphAddress;
import scraper.api.NodeAddress;
import scraper.api.FunctionalNodeContainer;
import scraper.api.NodeContainer;
import scraper.api.NodeLogLevel;
import scraper.api.FunctionalNode;
import scraper.api.Node;
import scraper.api.NodeHook;
import scraper.api.ScrapeInstance;
import scraper.api.T;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class MockContainer implements FunctionalNodeContainer {

    private FunctionalNode node;
    private Map<String, ?> spec;
    private String graphKey;

    public MockContainer(FunctionalNode node, Map<String, Object> spec) { this.node = node; this.spec = spec; }

    @Override
    public void setNodeConfiguration(@NotNull Map<String, ?> nodeConfiguration, @NotNull String instance, @NotNull String graphKey) {
        spec = nodeConfiguration;
    }

    @NotNull
    @Override public Map<String, ?> getNodeConfiguration() { return spec; }

    @NotNull
    @Override
    public Optional<?> getKeySpec(@NotNull String key) { return Optional.ofNullable(spec.get(key)); }

    @NotNull
    @Override
    public GraphAddress getGraphKey() {
        throw new IllegalStateException();
    }

    @NotNull
    @Override
    public NodeAddress getAddress() {
        throw new IllegalStateException();
    }

    @NotNull
    @Override
    public Optional<NodeContainer<? extends Node>> getGoTo() {
        throw new IllegalStateException();
    }

    @NotNull
    @Override
    public Collection<NodeHook> hooks() {
        throw new IllegalStateException();
    }

    @Override
    public boolean isForward() {
        return false;
    }

    @NotNull
    @Override
    public void forward(@NotNull FlowMap o) throws NodeException {
        throw new IllegalStateException();
    }

    @Override
    public void fork(FlowMap o) {

    }

    @Override
    public void forward(FlowMap o, Address target) {

    }

    @NotNull
    @Override
    public FunctionalNode getC() {
        throw new IllegalStateException();
    }

    @NotNull
    @Override
    public Address addressOf(@NotNull String representation) {
        return null;
    }

    @Override
    public void log(@NotNull NodeLogLevel trace, @NotNull String s, @NotNull Object... args) {

    }

    @NotNull
    @Override
    public ScrapeInstance getJobInstance() {
        throw new IllegalStateException();
    }

    @NotNull
    @Override
    public ExecutorService getService() {
        throw new IllegalStateException();
    }

    @Override
    public String getServiceGroup() {
        return null;
    }

    @Override
    public void init(@NotNull ScrapeInstance parent) throws ValidationException {

    }

    @Override
    public List<ValidationException> initWithErrors(ScrapeInstance parent) {
        return null;
    }

    @NotNull
    @Override
    public <A> A evalIdentity(@NotNull T<A> template) {
        return null;
    }
}
