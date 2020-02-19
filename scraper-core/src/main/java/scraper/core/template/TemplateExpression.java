package scraper.core.template;

import com.google.common.reflect.TypeToken;
import scraper.annotations.NotNull;
import scraper.api.flow.FlowMap;

import java.util.Collection;

public abstract class TemplateExpression<T> {

    @NotNull final TypeToken<T> targetType;

    TemplateExpression(@NotNull TypeToken<T> targetType) {
        this.targetType = targetType;
    }

    public abstract T eval(@NotNull final FlowMap o);

    @SuppressWarnings("unchecked") // get exact type for this type token, not the raw type, type token has no API if getType() to return Class<T> instead of Class<? super T>
    public @NotNull Class<T> getType() {
        return (Class<T>) targetType.getRawType();
    }

    public abstract @NotNull Collection<String> getKeysInTemplate(@NotNull FlowMap o);
}
