package scraper.api.reflect;

import java.util.Map;

public interface MapTerm<Y> extends Term<Map<String, Y>> {

    Map<String, Term<Y>> getTerms();
}
