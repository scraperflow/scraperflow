package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.template.MapLookup;
import scraper.api.template.T;
import scraper.api.template.TVisitor;
import scraper.api.template.Term;

import java.util.Map;

public class TemplateMapLookup<K> extends TemplateExpression<K> implements MapLookup<K> {
    @Override public void accept(@NotNull TVisitor visitor) { visitor.visitMapLookup(this); }

    private TemplateExpression<Map<String, ? extends K>> map;
    private TemplateExpression<String> key;

    public TemplateMapLookup(
            TemplateExpression<Map<String, ? extends K>> map,
            TemplateExpression<String> key,
            T<K> targetType) {
        super(targetType);
        this.map = map;
        this.key = key;
    }

    public K eval(@NotNull final FlowMap o) {
        try {
            Map<String, ? extends K> m = map.eval(o);
            String k = key.eval(o);

            K mapElement = m.get(k);

            if(mapElement == null) throw new TemplateException("Key '"+k+"' does not exist for map access '" +toString() +"'. Map has only the keys " + m.keySet()+"");

            return mapElement;
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
        return "{" + map.getRaw() + "}[" + key.getRaw() + "]";
    }

    @NotNull
    @Override
    public Term<Map<String, ? extends K>> getMapObjectTerm() {
        return map;
    }

    @NotNull
    @Override
    public Term<String> getKeyTerm() {
        return key;
    }
}
