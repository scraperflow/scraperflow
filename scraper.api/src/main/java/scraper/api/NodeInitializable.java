package scraper.api;


import scraper.annotations.NotNull;

import java.util.List;

/** A node has to be initialized once it can be used in a runtime specification */
public interface NodeInitializable {
    /** Initializes parsed node definition, throws exception on first error */
    void init(@NotNull ScrapeInstance parent) throws ValidationException;
    /** Initializes parsed node definition, returns all errors */
    List<ValidationException> initWithErrors(@NotNull ScrapeInstance parent);
}
