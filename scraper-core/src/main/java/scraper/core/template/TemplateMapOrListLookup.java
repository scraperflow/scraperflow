package scraper.core.template;

import com.google.common.reflect.TypeToken;
import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TemplateMapOrListLookup<T> extends TemplateExpression<T> {
    private TemplateExpression<List<T>> list;
    private TemplateExpression<Integer> index;
    private TemplateExpression<Map<String, T>> map;
    private TemplateExpression<String> key;

    public TemplateMapOrListLookup(
            TemplateExpression<Map<String, T>> map,
            TemplateExpression<List<T>> list,
            TemplateExpression<Integer> index,
            TemplateExpression<String> key,
            TypeToken<T> targetType) {
        super(targetType);
        this.map = map;
        this.list = list;
        this.index = index;
        this.key = key;
    }


    public T eval(@NotNull final FlowMap o) {
        try{
            List<T> l = list.eval(o);
            Integer index = this.index.eval(o);

            if (index < 0) {
                return l.get(l.size() - Math.abs(index));
            } else {
                return l.get(index);
            }
        } catch (Exception ignored) {}

        try {
            Map<String, T> m = map.eval(o);
            String k = key.eval(o);

            return m.get(k);
        } catch (Exception e) {
            throw new TemplateException("Could not evaluate array/map lookup template '"+toString()+"'. " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "{" + list.toString() + "}[" + index.toString() + "]";
    }

    public @NotNull Collection<String> getKeysInTemplate(@NotNull FlowMap o) {
        // TODO implement
        return null;
    }
}
