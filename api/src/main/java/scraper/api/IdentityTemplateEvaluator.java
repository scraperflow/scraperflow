package scraper.api.flow;


import scraper.annotations.NotNull;
import scraper.api.template.T;

/**
 * Class able to evaluate template identities
 */
public interface IdentityTemplateEvaluator {
    /** Evaluates the given template with identity mapping */
    @NotNull
    <A> A evalIdentity(@NotNull T<A> template);
}
