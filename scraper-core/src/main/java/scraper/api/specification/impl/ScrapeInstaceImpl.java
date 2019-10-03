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

import java.util.*;

public class ScrapeInstaceImpl implements ScrapeInstance {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ScrapeInstaceImpl.class);

    /** Name of the jobPojo */
    public String name = "NoName";

    /** Describes what the intent of the jobPojo is */
    public String description = "No description given";

    /** Parsed arguments of each process */
    public List<Map<String, Object>> process = new ArrayList<>();

    /** Generated nodes of the jobPojo */
    @JsonIgnore
    private List<Node> mainFlow = new ArrayList<>();

    /** Generated fragment nodes of the jobPojo */
    @JsonIgnore
    private List<List<Node>> fragmentFlows = new ArrayList<>();

    /** Initial input arguments */
    public Map<String, Object> initialArguments = new HashMap<>();

    /** Arguments applied to all nodes of given type */
    public Map<String, Map<String, Object>> all;

    /**
     * Gets the next node specified by {@code o}. If o is parsable as an integer, it is used as the next stage index.
     * Otherwise, its used as a goTo label.
     *
     * @param target Stage index or goTo label
     * @return Node goTo
     * @throws RuntimeException If node goTo can not be found
     */
    @JsonIgnore
    @Override
    public @NotNull Node getNode(@NotNull NodeAddress target) {
        for (Node node : mainFlow) {
            if(node.getAddress().equals(target))
                return node;
        }

        throw new IllegalArgumentException("Node address not existing! "+target);
    }

    @Override
    public NodeAddress getForwardTarget(NodeAddress origin) {
        for (int i = 0; i < mainFlow.size(); i++) {
            if(mainFlow.get(i).getAddress().equals(origin)) {
                if(i+1 < mainFlow.size()) {
                    return mainFlow.get(i+1).getAddress();
                }
            }
        }

        return null;
    }


    @JsonIgnore private ExecutorsService executors;
    @JsonIgnore private HttpService httpService;
    @JsonIgnore private ProxyReservation proxyReservation;
    @JsonIgnore private FileService fileService;

    @JsonIgnore
    public void init() throws ValidationException {
        log.info("Initializing main flow '{}'", getName());

        for (Node node : mainFlow)
            if (node instanceof NodeInitializable) ((NodeInitializable) node).init(this);

        log.info("Initializing fragments '{}'", getName());

        for (List<Node> fragmentFlow: fragmentFlows)
            for (Node node : fragmentFlow)
                if (node instanceof NodeInitializable) ((NodeInitializable) node).init(this);
    }


    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }


    public List<Map<String, Object>> getProcess() {
        return this.process;
    }

    public List<Node> getMainFlow() {
        return this.mainFlow;
    }

    @Override
    public List<List<Node>> getFragmentFlows() {
        return fragmentFlows;
    }

    public Map<String, Object> getInitialArguments() {
        return this.initialArguments;
    }

    @Override
    public Map<String, Map<String, Object>> getGlobalNodeConfigurations() {
        return all;
    }

    public Map<String, Map<String, Object>> getAll() {
        return this.all;
    }

    public ExecutorsService getExecutors() {
        return this.executors;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProcess(List<Map<String, Object>> process) {
        this.process = process;
    }

    public void setMainFlow(List<Node> flow) {
        this.mainFlow = mainFlow;
    }

    public void addFragmentFlow(List<Node> flow) {
        this.fragmentFlows.add(flow);
    }

    public void setInitialArguments(Map<String, Object> initialArguments) {
        this.initialArguments = initialArguments;
    }

    public void setAll(Map<String, Map<String, Object>> all) {
        this.all = all;
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
