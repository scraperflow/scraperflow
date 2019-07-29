package scraper.core.template;

import com.google.common.reflect.TypeToken;
import scraper.api.flow.FlowMap;

import java.util.Collection;

public abstract class TemplateExpression<T> {

    protected TypeToken<T> targetType;

    public TemplateExpression(TypeToken<T> targetType) {
        this.targetType = targetType;
    }


    public abstract T eval(final FlowMap o);

    public Class<T> getType() {
        //noinspection unchecked
        return (Class<T>) targetType.getRawType();
    }

    public abstract Collection<String> getKeysInTemplate(FlowMap o);
}
