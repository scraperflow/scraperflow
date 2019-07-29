package scraper.core.template;

import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import scraper.api.converter.StringToClassConverter;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;

import java.util.Collection;
import java.util.HashSet;

public class TemplateMapKey<T> extends TemplateExpression<T> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TemplateMapKey.class);

    private TemplateExpression<String> keyLookup;

    public TemplateMapKey(TemplateExpression<String> keyLookup, TypeToken<T> targetType) {
        super(targetType);
        this.keyLookup = keyLookup;
    }


    public T eval(final FlowMap o) {
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
        StringBuilder str = new StringBuilder();

        str.append("{");
        str.append(keyLookup.toString());
        str.append("}");

        return str.toString();
    }

    public Collection<String> getKeysInTemplate(FlowMap o) {
        Collection<String> allKeys = new HashSet<>(keyLookup.getKeysInTemplate(o));
        String key = keyLookup.eval(o);
        allKeys.add(key);
        return allKeys;
    }

    public static <C> TemplateMapKey<C> stringToMapKeyTemplate(String value, Class<C> rawType) {
        return null;
    }
}
