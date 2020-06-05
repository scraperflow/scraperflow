package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.flow.FlowMap;
import scraper.api.template.T;
import scraper.api.template.Term;

public abstract class TemplateExpression<K> implements Term<K> {

    @NotNull T<K> targetType;

    TemplateExpression(@NotNull T<K> targetType) { this.targetType = targetType; }

    @Override
    public @NotNull T<K> getToken() { return targetType; }

    @Override
    public void setToken(T<K> token) { targetType = token; }

    public abstract K eval(@NotNull FlowMap o);
}
