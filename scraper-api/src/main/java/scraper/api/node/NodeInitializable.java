package scraper.api.node;


import scraper.api.exceptions.ValidationException;
import scraper.api.specification.ScrapeInstance;

public interface NodeInitializable {
    /** Initializes parsed node definition */
    void init(ScrapeInstance parent) throws ValidationException;
}
