package scraper.core.template;

import com.google.common.reflect.TypeToken;
import scraper.annotations.NotNull;
import scraper.api.converter.StringToClassConverter;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;

import java.util.Collection;
import java.util.HashSet;

public class TemplateMapKey<T> extends TemplateExpression<T> {
    private TemplateExpression<String> keyLookup;

    public TemplateMapKey(@NotNull final TemplateExpression<String> keyLookup, @NotNull final TypeToken<T> targetType) {
        super(targetType);
        this.keyLookup = keyLookup;
    }

    public T eval(@NotNull final FlowMap o) {
        try{
            String targetKey = keyLookup.eval(o);
            if(!o.keySet().contains(targetKey))
                throw new IllegalStateException("FlowMap has no element at key " + targetKey);


            Object targetValue = o.get(targetKey);
            Object converted = StringToClassConverter.convert(targetValue, targetType.getRawType());
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
