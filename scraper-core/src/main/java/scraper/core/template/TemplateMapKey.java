package scraper.core.template;

import com.google.common.reflect.TypeToken;
import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

public class TemplateMapKey<T> extends TemplateExpression<T> {
    private TemplateExpression<String> keyLookup;

    public TemplateMapKey(@NotNull final TemplateExpression<String> keyLookup, @NotNull final TypeToken<T> targetType) {
        super(targetType);
        this.keyLookup = keyLookup;
    }

    public T eval(@NotNull final FlowMap o) {
        try{
            String targetKey = keyLookup.eval(o);
            Optional<T> targetValue = ((FlowMapImpl) o).getWithType(targetKey, targetType);

            if(targetValue.isEmpty())
                throw new IllegalStateException("FlowMap has no element at key " + targetKey);

            return targetValue.get();
        } catch (Exception e) {
            throw new TemplateException("Could not evaluate map key template '"+toString()+"'. "+ e.toString());
        }
    }

    @Override
    public String toString() {
        return "{" + keyLookup.toString() + "}";
    }

    @NotNull
    public Collection<String> getKeysInTemplate(@NotNull FlowMap o) {
        Collection<String> allKeys = new HashSet<>(keyLookup.getKeysInTemplate(o));
        String key = keyLookup.eval(o);
        allKeys.add(key);
        return allKeys;
    }
}
