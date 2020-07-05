package scraper.api.specification;

import scraper.annotations.NotNull;

import java.io.Serializable;

public interface ScrapeImportSpecification extends Serializable {
    @NotNull
    ScrapeSpecification getSpec();
}
