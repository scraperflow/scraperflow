package scraper.core.template;

import com.google.common.reflect.TypeToken;
import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.template.MapLookup;
import scraper.api.template.T;
import scraper.api.template.TVisitor;
import scraper.api.template.Term;

import java.util.Map;

public class TemplateMapLookup<K> extends TemplateExpression<K> implements MapLookup<K> {
    @Override public void accept(@NotNull TVisitor visitor) { visitor.visitMapLookup(this); }

    private TemplateExpression<Map<String, ?>> map;
    private TemplateExpression<String> key;

    public TemplateMapLookup(
            TemplateExpression<Map<String, ?>> map,
            TemplateExpression<String> key,
            T<K> targetType) {
        super(targetType);
        this.map = map;
        this.key = key;
    }

    @SuppressWarnings("unchecked") // checked with map generics subtype relation
    public K eval(@NotNull final FlowMap o) {
        try {
            Map<String, ?> m = map.eval(o);
            String k = key.eval(o);

            Object mapElement = m.get(k);

            if(mapElement == null)
                throw new TemplateException("Key '"+k+"' does not exist for map access '" +toString() +"'. Map has only the keys " + m.keySet()+"");


            TypeToken<?> known = FlowMapImpl.inferType(mapElement);
            TypeToken<?> target = TypeToken.of(targetType.get());

            FlowMapImpl.checkGenericType(known, target);

            return (K) mapElement;
        } catch (Exception e) {
            throw new TemplateException(e, "Could not evaluate array/map lookup template '"+toString()+"'. " + e.getMessage());
        }
    }

    @NotNull
    @Override
    public Object getRaw() {
        return toString();
    }

    @Override
    public String toString() {
        return "{" + map.getRaw() + "@" + key.getRaw()+ "}";
    }

    @NotNull
    @Override
    public Term<Map<String, ?>> getMapObjectTerm() {
        return map;
    }

    @NotNull
    @Override
    public Term<String> getKeyTerm() {
        return key;
    }
}
