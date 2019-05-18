package scraper.api.specification.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import scraper.api.node.Node;
import scraper.api.exceptions.ValidationException;
import scraper.api.specification.ScrapeInstance;
import scraper.api.node.NodeInitializable;
import scraper.api.service.ExecutorsService;
import scraper.api.service.FileService;
import scraper.api.service.HttpService;
import scraper.api.service.ProxyReservation;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ScrapeInstaceImpl implements ScrapeInstance {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ScrapeInstaceImpl.class);

    /** Name of the jobPojo */
    public String name = "NoName";

    /** Describes what the intent of the jobPojo is */
    public String description = "No description given";

    /** Parsed arguments of each process */
    public List<Map<String, Object>> process = new ArrayList<>();

    /** Generated process nodes of the jobPojo */
    @JsonIgnore
    public List<Node> jobProcess = new ArrayList<>();

    /** Initial input arguments */
    public Map<String, Object> initialArguments = new HashMap<>();

    /** Arguments applied to all nodes of given type */
    public Map<String, Map<String, Object>> all;

    /** Current running tasks which stop JVM from exit. Periodically emptied by main application */
    @JsonIgnore
    public List<CompletableFuture<Void>> uncompletedTasks = Collections.synchronizedList(new ArrayList<>());

    /**
     * Gets the next node specified by {@code o}. If o is parsable as an integer, it is used as the next stage index.
     * Otherwise, its used as a target label.
     *
     * @param target Stage index or target label
     * @return Node target
     * @throws RuntimeException If node target can not be found
     */
    @JsonIgnore
    @Override
    public Node getProcessNode(String target) {
        try { // try index
            int index = Integer.parseInt(target);
            return jobProcess.get(index);
        } catch (Exception e) { // try label
            for (Node node : jobProcess) {
                if(node.getLabel() != null && node.getLabel().equalsIgnoreCase(target))
                    return node;
            }
        }

        log.error("Argument is neither an index nor a label: {}", target);
        throw new RuntimeException("Argument is neither an index nor a label! "+target);
    }

    @JsonIgnore
    @Override
    public Map<String, Object> getProcessNodeDefinition(String target) {
        try { // try index
            int index = Integer.parseInt(target);
            return process.get(index);
        } catch (Exception e) { // try label
            for (Map<String, Object> nodeDefinition : process) {
                String label = (String) nodeDefinition.get("label");
                if(label != null && label.equalsIgnoreCase(target))
                    return nodeDefinition;
            }
        }

        log.error("Argument is neither an index nor a label: {}", target);
        throw new RuntimeException("Argument is neither an index nor a label! "+target);
    }

    /**
     * Fetches one value for the key of the specified node.
     * @param stageIndex Target node
     * @param key Key in the node definition
     * @return Value of key in the node definition; null if not defined in .scrape file
     */
    @Override
    @JsonIgnore
    public Object getProcessKey(int stageIndex, String key) {
        return process.get(stageIndex).getOrDefault(key, null);
    }

    /**
     * Fetches the argument map for the specified node.
     * @param stageIndex Target node
     * @return Parsed argument map
     */
    @Override
    @JsonIgnore
    public Map<String, Object> getProcessKeys(int stageIndex) {
        return process.get(stageIndex);
    }

    /** Adds a new node to the jobPojo */
    public synchronized void addProcessNode(Node node) {
        this.jobProcess.add(node);
    }


    @JsonIgnore private ExecutorsService executors;
    @JsonIgnore private HttpService httpService;
    @JsonIgnore private ProxyReservation proxyReservation;
    @JsonIgnore private FileService fileService;

    @JsonIgnore
    public void init() throws ValidationException {
        log.info("Initializing '{}'", getName());

        for (Node node : jobProcess)
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

    public List<Node> getJobProcess() {
        return this.jobProcess;
    }

    public Map<String, Object> getInitialArguments() {
        return this.initialArguments;
    }

    public Map<String, Map<String, Object>> getAll() {
        return this.all;
    }

    public List<CompletableFuture<Void>> getUncompletedTasks() {
        return this.uncompletedTasks;
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

    public void setJobProcess(List<Node> jobProcess) {
        this.jobProcess = jobProcess;
    }

    public void setInitialArguments(Map<String, Object> initialArguments) {
        this.initialArguments = initialArguments;
    }

    public void setAll(Map<String, Map<String, Object>> all) {
        this.all = all;
    }

    public void setUncompletedTasks(List<CompletableFuture<Void>> uncompletedTasks) {
        this.uncompletedTasks = uncompletedTasks;
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
