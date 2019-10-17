package scraper.api.specification.impl;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.node.Address;
import scraper.api.node.GraphAddress;
import scraper.api.specification.ScrapeSpecification;
import scraper.util.NodeUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScrapeSpecificationImpl implements ScrapeSpecification {
    private String name;
    private Path scrapeFile;
    private List<Path> paths = new ArrayList<>();
    private String dependencies;
    private List<String> arguments = List.of();
    private Map<String, List<Address>> imports = Map.of();
    private GraphAddress entry = NodeUtil.graphAddressOf("start");
    private Map<GraphAddress, List<Map<String, Object>>> graphs;
    private Map<String, Map<String, Object>> globalNodeConfigurations = new HashMap<>();

    @NotNull
    @Override public String getName() { return this.name; }
    @NotNull
    @Override public Path getScrapeFile() { return this.scrapeFile; }
    @NotNull
    @Override public List<Path> getPaths() {
        if(!paths.contains(scrapeFile) && scrapeFile.getParent() != null) {
            paths.add(scrapeFile.getParent());
        }
        return this.paths;
    }
    @Nullable
    @Override public String getDependencies() { return dependencies; }
    @NotNull
    @Override public List<String> getArguments() { return arguments; }
    @NotNull
    @Override public Map<String, List<Address>> getImports() { return imports; }
    @NotNull
    @Override public GraphAddress getEntry() { return entry; }

    @NotNull
    @JsonDeserialize(keyUsing = GraphAddressKeyDeserializer.class)
    @Override public Map<GraphAddress, List<Map<String, Object>>> getGraphs() { return graphs; }

    public void setName(String name) { this.name = name; }
    public void setScrapeFile(Path scrapeFile) { this.scrapeFile = scrapeFile; }
    public void setPaths(List<Path> paths) { this.paths = paths; }
    public void setDependencies(String dependencies) { this.dependencies = dependencies; }
    public void setArguments(List<String> arguments) { this.arguments = arguments; }
    public void setImports(Map<String, List<Address>> imports) { this.imports = imports; }
    public void setEntry(GraphAddress entry) { this.entry = entry; }
    public void setGraphs(Map<GraphAddress, List<Map<String, Object>>> graphs) { this.graphs = graphs; }

    @NotNull
    @Override
    public Map<String, Map<String, Object>> getGlobalNodeConfigurations() {
        return globalNodeConfigurations;
    }

    public void setGlobalNodeConfigurations(Map<String, Map<String, Object>> globalNodeConfigurations) {
        this.globalNodeConfigurations = globalNodeConfigurations;
    }
}
