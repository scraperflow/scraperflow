package scraper.api;

import scraper.annotations.NotNull;

import java.io.Serializable;

public interface ScrapeImportSpecification extends Serializable {
    @NotNull
    ScrapeSpecification getSpec();
}
