package scraper.api.specification.impl;


import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.specification.ScrapeImportSpecification;
import scraper.api.specification.ScrapeSpecification;

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
    private Map<String, ScrapeImportSpecification> imports = Map.of();
    private String entry = "start";
    private Map<String, List<Map<String, Object>>> graphs;
    private Map<String, Map<String, Object>> globalNodeConfigurations = new HashMap<>();
    private Map<String, Object> initialArguments = new HashMap<>();


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
    @Override public Map<String, ScrapeImportSpecification> getImports() { return imports; }
    @NotNull
    @Override public String getEntry() { return entry; }

    @NotNull
    @Override public Map<String, List<Map<String, Object>>> getGraphs() { return graphs; }

    public void setName(String name) { this.name = name; }
    public void setScrapeFile(Path scrapeFile) { this.scrapeFile = scrapeFile; }
    public void setPaths(List<Path> paths) { this.paths = paths; }
    public void setDependencies(String dependencies) { this.dependencies = dependencies; }
    public void setArguments(List<String> arguments) { this.arguments = arguments; }
    public void setImports(Map<String, ScrapeImportSpecification> imports) { this.imports = imports; }
    public void setEntry(String entry) { this.entry = entry; }
    public void setGraphs(Map<String, List<Map<String, Object>>> graphs) { this.graphs = graphs; }
    public void setInitialarguments(Map<String, Object> args) { this.initialArguments = args; }

    @NotNull
    @Override
    public Map<String, Map<String, Object>> getGlobalNodeConfigurations() {
        return globalNodeConfigurations;
    }

    @Override
    public Map<String, Object> getInitialArguments() {
        return this.initialArguments;
    }

    public void setGlobalNodeConfigurations(Map<String, Map<String, Object>> globalNodeConfigurations) {
        this.globalNodeConfigurations = globalNodeConfigurations;
    }
}
