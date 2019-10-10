package scraper.api.specification.impl;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import scraper.api.node.NodeAddress;
import scraper.api.specification.ScrapeSpecification;
import scraper.util.NodeUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScrapeSpecificationImpl implements ScrapeSpecification {
    private String name;
    private Path scrapeFile;
    private List<Path> paths = new ArrayList<>();
    private String dependencies;
    private List<String> arguments = List.of();
    private Map<String, String> imports = Map.of();
    private NodeAddress entry = NodeUtil.addressOf("start");
    private Map<NodeAddress, List<Map<String, Object>>> graphs;
    private Map<String, Map<String, Object>> globalNodeConfigurations = Map.of();

    @Override public String getName() { return this.name; }
    @Override public Path getScrapeFile() { return this.scrapeFile; }
    @Override public List<Path> getPaths() {
        if(!paths.contains(scrapeFile) && scrapeFile.getParent() != null) {
            paths.add(scrapeFile.getParent());
        }
        return this.paths;
    }
    @Override public String getDependencies() { return dependencies; }
    @Override public List<String> getArguments() { return arguments; }
    @Override public Map<String, String> getImports() { return imports; }
    @Override public NodeAddress getEntry() { return entry; }

    @JsonDeserialize(keyUsing = NodeAddressKeyDeserializer.class)
    @Override public Map<NodeAddress, List<Map<String, Object>>> getGraphs() { return graphs; }

    public void setName(String name) { this.name = name; }
    public void setScrapeFile(Path scrapeFile) { this.scrapeFile = scrapeFile; }
    public void setPaths(List<Path> paths) { this.paths = paths; }
    public void setDependencies(String dependencies) { this.dependencies = dependencies; }
    public void setArguments(List<String> arguments) { this.arguments = arguments; }
    public void setImports(Map<String, String> imports) { this.imports = imports; }
    public void setEntry(NodeAddress entry) { this.entry = entry; }
    public void setGraphs(Map<NodeAddress, List<Map<String, Object>>> graphs) { this.graphs = graphs; }

    @Override
    public Map<String, Map<String, Object>> getGlobalNodeConfigurations() {
        return globalNodeConfigurations;
    }

    public void setGlobalNodeConfigurations(Map<String, Map<String, Object>> globalNodeConfigurations) {
        this.globalNodeConfigurations = globalNodeConfigurations;
    }
}
