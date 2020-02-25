package scraper.api.reflect;

import java.util.List;

public interface ListTerm<Y> extends Term<List<Y>> {
    List<Term<Y>> getTerms();

    T<Y> getElementToken();
}
