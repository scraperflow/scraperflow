package scraper.api.specification.impl;


import scraper.api.specification.ScrapeSpecification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScrapeSpecificationImpl implements ScrapeSpecification {
    // unique name of the job
    private final String name;
    private final String basePath;
    
    private final String scrapeFile;

    private final String nodeDependencyFile;
    private final String controlFlowGraphFile;
    private final String packagedFile;

    private List<String> paths;

    private List<String> fragmentFolders;
    private List<String> argumentFiles;

    ScrapeSpecificationImpl(String name, String basePath, String scrapeFile, String nodeDependencyFile, String controlFlowGraphFile, String packagedFile, List<String> paths, List<String> fragmentFolders, List<String> argumentFiles) {
        this.name = name;
        this.basePath = basePath;
        this.scrapeFile = scrapeFile;
        this.nodeDependencyFile = nodeDependencyFile;
        this.controlFlowGraphFile = controlFlowGraphFile;
        this.packagedFile = packagedFile;
        this.paths = paths;
        this.fragmentFolders = fragmentFolders;
        this.argumentFiles = argumentFiles;
    }

    public static JobDefinitionBuilder builder() {
        return new JobDefinitionBuilder();
    }

    public String getName() {
        return this.name;
    }

    public String getBasePath() {
        return this.basePath;
    }

    public String getScrapeFile() {
        return this.scrapeFile;
    }

    public String getNodeDependencyFile() {
        return this.nodeDependencyFile;
    }

    public String getControlFlowGraphFile() {
        return this.controlFlowGraphFile;
    }

    public String getPackagedFile() {
        return this.packagedFile;
    }

    public List<String> getPaths() {
        return this.paths;
    }

    public List<String> getFragmentFolders() {
        return this.fragmentFolders;
    }

    public List<String> getArgumentFiles() {
        return this.argumentFiles;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public void setFragmentFolders(List<String> fragmentFolders) {
        this.fragmentFolders = fragmentFolders;
    }

    public void setArgumentFiles(List<String> argumentFiles) {
        this.argumentFiles = argumentFiles;
    }

    public static class JobDefinitionBuilder {
        private String name;
        private String basePath;
        private String scrapeFile;
        private String nodeDependencyFile;
        private String controlFlowGraphFile;
        private String packagedFile;
        private ArrayList<String> paths;
        private ArrayList<String> fragmentFolders;
        private ArrayList<String> argumentFiles;

        public JobDefinitionBuilder() {
        }

        public ScrapeSpecificationImpl.JobDefinitionBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ScrapeSpecificationImpl.JobDefinitionBuilder basePath(String basePath) {
            this.basePath = basePath;
            return this;
        }

        public ScrapeSpecificationImpl.JobDefinitionBuilder scrapeFile(String scrapeFile) {
            this.scrapeFile = scrapeFile;
            return this;
        }

        public ScrapeSpecificationImpl.JobDefinitionBuilder nodeDependencyFile(String nodeDependencyFile) {
            this.nodeDependencyFile = nodeDependencyFile;
            return this;
        }

        public ScrapeSpecificationImpl.JobDefinitionBuilder controlFlowGraphFile(String controlFlowGraphFile) {
            this.controlFlowGraphFile = controlFlowGraphFile;
            return this;
        }

        public ScrapeSpecificationImpl.JobDefinitionBuilder packagedFile(String packagedFile) {
            this.packagedFile = packagedFile;
            return this;
        }

        public ScrapeSpecificationImpl.JobDefinitionBuilder path(String path) {
            if (this.paths == null) this.paths = new ArrayList<String>();
            this.paths.add(path);
            return this;
        }

        public ScrapeSpecificationImpl.JobDefinitionBuilder paths(Collection<? extends String> paths) {
            if (this.paths == null) this.paths = new ArrayList<String>();
            this.paths.addAll(paths);
            return this;
        }

        public ScrapeSpecificationImpl.JobDefinitionBuilder clearPaths() {
            if (this.paths != null)
                this.paths.clear();

            return this;
        }

        public ScrapeSpecificationImpl.JobDefinitionBuilder fragmentFolder(String fragmentFolder) {
            if (this.fragmentFolders == null) this.fragmentFolders = new ArrayList<String>();
            this.fragmentFolders.add(fragmentFolder);
            return this;
        }

        public ScrapeSpecificationImpl.JobDefinitionBuilder fragmentFolders(Collection<? extends String> fragmentFolders) {
            if (this.fragmentFolders == null) this.fragmentFolders = new ArrayList<String>();
            this.fragmentFolders.addAll(fragmentFolders);
            return this;
        }

        public ScrapeSpecificationImpl.JobDefinitionBuilder clearFragmentFolders() {
            if (this.fragmentFolders != null)
                this.fragmentFolders.clear();

            return this;
        }

        public ScrapeSpecificationImpl.JobDefinitionBuilder argumentFile(String argumentFile) {
            if (this.argumentFiles == null) this.argumentFiles = new ArrayList<String>();
            this.argumentFiles.add(argumentFile);
            return this;
        }

        public ScrapeSpecificationImpl.JobDefinitionBuilder argumentFiles(Collection<? extends String> argumentFiles) {
            if (this.argumentFiles == null) this.argumentFiles = new ArrayList<String>();
            this.argumentFiles.addAll(argumentFiles);
            return this;
        }

        public ScrapeSpecificationImpl.JobDefinitionBuilder clearArgumentFiles() {
            if (this.argumentFiles != null)
                this.argumentFiles.clear();

            return this;
        }

        public ScrapeSpecificationImpl build() {
            List<String> paths;
            switch (this.paths == null ? 0 : this.paths.size()) {
                case 0:
                    paths = java.util.Collections.emptyList();
                    break;
                case 1:
                    paths = java.util.Collections.singletonList(this.paths.get(0));
                    break;
                default:
                    paths = java.util.Collections.unmodifiableList(new ArrayList<String>(this.paths));
            }
            List<String> fragmentFolders;
            switch (this.fragmentFolders == null ? 0 : this.fragmentFolders.size()) {
                case 0:
                    fragmentFolders = java.util.Collections.emptyList();
                    break;
                case 1:
                    fragmentFolders = java.util.Collections.singletonList(this.fragmentFolders.get(0));
                    break;
                default:
                    fragmentFolders = java.util.Collections.unmodifiableList(new ArrayList<String>(this.fragmentFolders));
            }
            List<String> argumentFiles;
            switch (this.argumentFiles == null ? 0 : this.argumentFiles.size()) {
                case 0:
                    argumentFiles = java.util.Collections.emptyList();
                    break;
                case 1:
                    argumentFiles = java.util.Collections.singletonList(this.argumentFiles.get(0));
                    break;
                default:
                    argumentFiles = java.util.Collections.unmodifiableList(new ArrayList<String>(this.argumentFiles));
            }

            return new ScrapeSpecificationImpl(name, basePath, scrapeFile, nodeDependencyFile, controlFlowGraphFile, packagedFile, paths, fragmentFolders, argumentFiles);
        }

        public String toString() {
            return "ScrapeSpecificationImpl.JobDefinitionBuilder(name=" + this.name + ", basePath=" + this.basePath + ", scrapeFile=" + this.scrapeFile + ", nodeDependencyFile=" + this.nodeDependencyFile + ", controlFlowGraphFile=" + this.controlFlowGraphFile + ", packagedFile=" + this.packagedFile + ", paths=" + this.paths + ", fragmentFolders=" + this.fragmentFolders + ", argumentFiles=" + this.argumentFiles + ")";
        }
    }
}
