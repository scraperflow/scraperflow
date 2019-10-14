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

    public @NotNull Class<T> getType() {
        //noinspection unchecked
        return (Class<T>) targetType.getRawType();
    }

    public abstract @NotNull Collection<String> getKeysInTemplate(@NotNull FlowMap o);
}
