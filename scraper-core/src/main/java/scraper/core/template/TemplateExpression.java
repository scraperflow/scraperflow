package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.flow.FlowMap;
import scraper.api.template.T;
import scraper.api.template.Term;

public abstract class TemplateExpression<K> implements Term<K> {

    @NotNull final T<K> targetType;

    TemplateExpression(@NotNull T<K> targetType) { this.targetType = targetType; }

    @Override
    public @NotNull T<K> getToken() { return targetType; }

    public abstract K eval(@NotNull FlowMap o);
}
