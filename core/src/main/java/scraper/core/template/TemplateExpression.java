package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.FlowMap;
import scraper.api.T;
import scraper.api.Term;

public abstract class TemplateExpression<K> implements Term<K> {

    @NotNull T<K> targetType;
    protected String typevarsuffix;

    TemplateExpression(@NotNull T<K> targetType) { this.targetType = targetType; }

    @Override
    public @NotNull T<K> getToken() { return targetType; }

    @Override
    public void setToken(T<K> token) { targetType = token; }

    public abstract K eval(@NotNull FlowMap o);

    @Override
    public Term<K> withTypeVar(String typevarsuffix) {
        this.typevarsuffix = typevarsuffix;
        return this;
    }
}
