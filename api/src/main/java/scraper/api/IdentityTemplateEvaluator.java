package scraper.api;


import scraper.annotations.NotNull;

/**
 * Class able to evaluate template identities
 */
public interface IdentityTemplateEvaluator {
    /** Evaluates the given template with identity mapping */
    @NotNull
    <A> A evalIdentity(@NotNull T<A> template);
}
