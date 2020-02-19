package scraper.core.template;

import com.google.common.reflect.TypeToken;
import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.core.converter.StringToClassConverter;

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
            Optional<?> targetValue = o.get(targetKey);

            if(targetValue.isEmpty())
                throw new IllegalStateException("FlowMap has no element at key " + targetKey);

            Object converted = StringToClassConverter.convert(targetValue.get(), targetType.getRawType());
            T returnt = (T) targetType.getRawType().cast(converted);

            return returnt;
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
