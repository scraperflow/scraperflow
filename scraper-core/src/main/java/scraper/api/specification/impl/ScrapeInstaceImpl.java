package scraper.api.specification.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import scraper.annotations.NotNull;
import scraper.api.node.Node;
import scraper.api.exceptions.ValidationException;
import scraper.api.node.NodeAddress;
import scraper.api.specification.ScrapeInstance;
import scraper.api.node.NodeInitializable;
import scraper.api.service.ExecutorsService;
import scraper.api.service.FileService;
import scraper.api.service.HttpService;
import scraper.api.service.ProxyReservation;
import scraper.util.NodeUtil;

import java.util.*;
import java.util.function.BiConsumer;

public class ScrapeInstaceImpl implements ScrapeInstance {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ScrapeInstaceImpl.class);

    /** Name of the jobPojo */
    public String name = "NoName";

    /** Describes what the intent of the jobPojo is */
    public String description = "No description given";

    public NodeAddress entry = NodeUtil.addressOf("start");

    /** Generated nodes of the jobPojo */
    private Map<NodeAddress, List<Node>> graphs = new HashMap<>();

    /** Initial input arguments */
    public Map<String, Object> initialArguments = new HashMap<>();

    /** Arguments applied to all nodes of given type */
    public Map<String, Map<String, Object>> globalNodeConfigurations = new HashMap<>();

    /**
     * Gets the next node specified by {@code o}. If o is parsable as an integer, it is used as the next stage index.
     * Otherwise, its used as a goTo label.
     *
     * @param target Stage index or goTo label
     * @return Node goTo
     * @throws RuntimeException If node goTo can not be found
     */

    @Override
    public @NotNull Node getNode(@NotNull NodeAddress target) {

        for (NodeAddress k : graphs.keySet()) {
            if(k.equals(target)) {
                return graphs.get(k).get(0);
            }

            for (Node node : graphs.get(k)) {
                if(node.getAddress().equals(target))
                    return node;
            }
        }

        throw new IllegalArgumentException("Node address not existing! "+target);
    }

    @Override
    public NodeAddress getForwardTarget(@NotNull NodeAddress origin) {
        for (NodeAddress k : graphs.keySet()) {
            if(k.equals(origin)) {
                return graphs.get(k).get(0).getAddress();
            }

            Iterator<Node> it = graphs.get(k).iterator();
            while(it.hasNext()) {
                Node node = it.next();
                if(node.getAddress().equals(origin)){
                    if(it.hasNext()) {
                        return it.next().getAddress();
                    } else {
                        return null;
                    }
                }
            }
        }

        throw new IllegalStateException("Origin node address not found in any graph");
    }

    @NotNull
    @Override
    public Map<NodeAddress, List<Node>> getGraphs() {
        return graphs;
    }

    @NotNull
    @Override
    public List<Node> getEntryGraph() {
        return getGraph(entry);
    }

    @Override
    public @NotNull List<Node> getGraph(@NotNull final NodeAddress address) {
        getGraphs().putIfAbsent(address, new ArrayList<>());
        return getGraphs().get(address);
    }


    private ExecutorsService executors;
    private HttpService httpService;
    private ProxyReservation proxyReservation;
    private FileService fileService;

    public void init() throws ValidationException {
        log.info("Initializing graphs '{}'", getName());
        for (NodeAddress k : getGraphs().keySet()) {
            for (Node node : getGraph(k)) {
                if (node instanceof NodeInitializable) ((NodeInitializable) node).init(this);
            }
        }
    }


    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    @NotNull
    public Map<String, Object> getInitialArguments() {
        return this.initialArguments;
    }

    @NotNull
    @Override
    public Map<String, Map<String, Object>> getGlobalNodeConfigurations() {
        return globalNodeConfigurations;
    }

    public ExecutorsService getExecutors() {
        return this.executors;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEntry(NodeAddress entryGraph) {
        this.entry = entryGraph;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setInitialArguments(Map<String, Object> initialArguments) {
        this.initialArguments = initialArguments;
    }

    public void setGlobalNodeConfigurations(Map<String, Map<String, Object>> globalNodeConfigurations) {
        this.globalNodeConfigurations = globalNodeConfigurations;
    }

    public void setExecutors(ExecutorsService executors) {
        this.executors = executors;
    }

    public HttpService getHttpService() {
        return httpService;
    }

    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    public ProxyReservation getProxyReservation() {
        return proxyReservation;
    }

    public void setProxyReservation(ProxyReservation proxyReservation) {
        this.proxyReservation = proxyReservation;
    }

    public FileService getFileService() {
        return fileService;
    }

    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }
}
