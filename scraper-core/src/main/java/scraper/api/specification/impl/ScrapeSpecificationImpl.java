package scraper.api.specification.impl;


import scraper.api.node.NodeAddress;
import scraper.api.specification.ScrapeSpecification;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ScrapeSpecificationImpl implements ScrapeSpecification {
    private String name;
    private Path scrapeFile;
    private List<Path> paths;
    private String dependencies;
    private List<String> arguments;
    private Map<String, String> imports;
    private List<NodeAddress> entries;
    private Map<String, List<Map<String, Object>>> graphs;

    @Override public String getName() { return this.name; }
    @Override public Path getScrapeFile() { return this.scrapeFile; }
    @Override public List<Path> getPaths() { return this.paths; }
    @Override public String getDependencies() { return dependencies; }
    @Override public List<String> getArguments() { return arguments; }
    @Override public Map<String, String> getImports() { return imports; }
    @Override public List<NodeAddress> getEntries() { return entries; }
    @Override public Map<String, List<Map<String, Object>>> getGraphs() { return graphs; }

    public void setName(String name) { this.name = name; }
    public void setScrapeFile(Path scrapeFile) { this.scrapeFile = scrapeFile; }
    public void setPaths(List<Path> paths) { this.paths = paths; }
    public void setDependencies(String dependencies) { this.dependencies = dependencies; }
    public void setArguments(List<String> arguments) { this.arguments = arguments; }
    public void setImports(Map<String, String> imports) { this.imports = imports; }
    public void setEntries(List<NodeAddress> entries) { this.entries = entries; }
    public void setGraphs(Map<String, List<Map<String, Object>>> graphs) { this.graphs = graphs; }
}
