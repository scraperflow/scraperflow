package scraper.api.template;

import scraper.annotations.NotNull;

import java.util.List;

public interface ListTerm<Y> extends Term<List<Y>> {

    T<Y> getElementType();
    @NotNull List<Term<Y>> getTerms();
    boolean isTypeVariable();
}
