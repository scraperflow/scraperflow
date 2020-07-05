package scraper.core;

import scraper.annotations.NotNull;
import scraper.api.flow.IdentityTemplateEvaluator;
import scraper.api.template.T;
import scraper.api.flow.impl.IdentityFlowMap;


/**
 */
public class IdentityEvaluator implements IdentityTemplateEvaluator {
    @NotNull
    @Override
    public <A> A evalIdentity(@NotNull T<A> template) {
        return template.getTerm().eval(new IdentityFlowMap());
    }
}
