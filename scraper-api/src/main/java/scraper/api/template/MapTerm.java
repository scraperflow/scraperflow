package scraper.api.template;

import scraper.annotations.NotNull;

import java.util.Map;

public interface MapTerm<Y> extends Term<Map<String, Y>> {
    T<Y> getElementType();

    boolean isTypeVariable();
    @NotNull Map<String, Term<Y>> getTerms();
}
