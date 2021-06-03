package scraper.api.specification.impl;

import scraper.annotations.NotNull;
import scraper.api.ScrapeImportSpecification;
import scraper.api.ScrapeSpecification;


public class ScraperImportSpecificationImpl implements ScrapeImportSpecification {

    private ScrapeSpecification spec;

    public ScraperImportSpecificationImpl() {}

    public ScraperImportSpecificationImpl(ScrapeSpecification imp) {
        this.spec = imp;
    }

    @NotNull
    @Override
    public ScrapeSpecification getSpec() {
        return spec;
    }

    public void setSpec(ScrapeSpecification spec) {
        this.spec = spec;
    }
}
