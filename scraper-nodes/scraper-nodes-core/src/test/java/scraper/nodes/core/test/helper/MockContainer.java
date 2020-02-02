package scraper.nodes.core.test.helper;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.exceptions.NodeException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.api.node.GraphAddress;
import scraper.api.node.NodeAddress;
import scraper.api.node.NodeHook;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.container.NodeLogLevel;
import scraper.api.node.type.FunctionalNode;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class MockContainer implements FunctionalNodeContainer {

    private FunctionalNode node;
    private Map<String, Object> spec;
    private String graphKey;

    public MockContainer(FunctionalNode node, Map<String, Object> spec) { this.node = node; this.spec = spec; }

    @Override
    public void setNodeConfiguration(Map<String, Object> nodeConfiguration, String instance, String graphKey) {
        spec = nodeConfiguration;
    }

    @NotNull @Override public Map<String, Object> getNodeConfiguration() { return spec; }

    @Nullable
    @Override
    public Object getKeySpec(@NotNull String key) { return spec.get(key); }

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

    @Nullable
    @Override
    public Optional<NodeContainer<? extends Node>> getGoTo() {
        throw new IllegalStateException();
    }

    @NotNull
    @Override
    public Collection<NodeHook> beforeHooks() {
        throw new IllegalStateException();
    }

    @NotNull
    @Override
    public Collection<NodeHook> afterHooks() {
        throw new IllegalStateException();
    }

    @Override
    public boolean isForward() {
        return false;
    }

    @NotNull
    @Override
    public FlowMap forward(@NotNull FlowMap o) throws NodeException {
        throw new IllegalStateException();
    }

    @NotNull
    @Override
    public FlowMap eval(@NotNull FlowMap o, @NotNull Address target) throws NodeException {
        throw new IllegalStateException();
    }

    @NotNull
    @Override
    public CompletableFuture<FlowMap> forkDepend(@NotNull FlowMap o, @NotNull Address target) {
        throw new IllegalStateException();
    }

    @Override
    public void forkDispatch(@NotNull FlowMap o, @NotNull Address target) {

    }

    @Override
    public FunctionalNode getC() {
        throw new IllegalStateException();
    }

    @Override
    public void log(NodeLogLevel trace, String s, Object... args) {

    }

    @Override
    public ScrapeInstance getJobInstance() {
        throw new IllegalStateException();
    }

    @Override
    public ExecutorService getService() {
        throw new IllegalStateException();
    }

    @Override
    public void init(@NotNull ScrapeInstance parent) throws ValidationException {

    }
}
