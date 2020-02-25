package scraper.core.template;

import com.google.common.reflect.TypeToken;
import scraper.annotations.NotNull;
import scraper.api.flow.FlowMap;
import scraper.api.reflect.T;
import scraper.api.reflect.Term;

import java.util.Map;

public abstract class TemplateExpression<K> implements Term<K> {

    @NotNull final T<K> targetType;

    TemplateExpression(@NotNull T<K> targetType) {
        this.targetType = targetType;
    }

    public abstract K eval(@NotNull final FlowMap o);

    @SuppressWarnings("unchecked") // get exact type for this type token, not the raw type, type token has no API if getType() to return Class<K> instead of Class<? super T>
    public @NotNull Class<K> getType() { return (Class<K>) targetType.get(); }

    public abstract @NotNull Map<String, T<?>> getKeysInTemplate(@NotNull FlowMap o);

    public T<K> getTarget() { return targetType; }
    @Override
    public T<K> getToken() { return targetType; }

}
