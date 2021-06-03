package scraper.api.node;


import scraper.annotations.NotNull;
import scraper.api.ValidationException;
import scraper.api.specification.ScrapeInstance;

/** A node has to be initialized once it can be used in a runtime specification */
public interface NodeInitializable {
    /** Initializes parsed node definition */
    void init(@NotNull ScrapeInstance parent) throws ValidationException;
}
