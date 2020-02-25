package scraper.api.specification;

import java.io.Serializable;

public interface ScrapeImportSpecification extends Serializable {
    ScrapeSpecification getSpec();

    void setSpec(ScrapeSpecification spec);
}
