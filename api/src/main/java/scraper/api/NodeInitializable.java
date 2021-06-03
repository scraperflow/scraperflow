package scraper.api;


import scraper.annotations.NotNull;

/** A node has to be initialized once it can be used in a runtime specification */
public interface NodeInitializable {
    /** Initializes parsed node definition */
    void init(@NotNull ScrapeInstance parent) throws ValidationException;
}
