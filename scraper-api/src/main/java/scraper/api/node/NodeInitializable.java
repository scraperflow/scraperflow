package scraper.api.node;


import scraper.annotations.NotNull;
import scraper.api.exceptions.ValidationException;
import scraper.api.specification.ScrapeInstance;

import java.util.Map;

public interface NodeInitializable {
    /** Initializes parsed node definition */
    void init(@NotNull ScrapeInstance parent) throws ValidationException;
}
