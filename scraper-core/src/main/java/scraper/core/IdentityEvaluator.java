package scraper.core;

import scraper.api.reflect.IdentityTemplateEvaluator;
import scraper.api.reflect.T;
import scraper.utils.IdentityFlowMap;


/**
 */
public class IdentityEvaluator implements IdentityTemplateEvaluator {
    @Override
    public <A> A evalIdentity(T<A> template) {
        return Template.eval(template, new IdentityFlowMap());
    }
}
