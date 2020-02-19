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
        }
        catch (IndexOutOfBoundsException e) {
            throw new TemplateException("Array index out of bounds for '"+toString()+"': "+ e.getMessage());
        }
        catch (Exception ignored) {}

        try {
            Map<String, T> m = map.eval(o);
            String k = key.eval(o);

            T mapElement = m.get(k);

            if(mapElement == null) throw new TemplateException("Key '"+k+"' does not exist for map access '" +toString() +"'. Map has only the keys " + m.keySet()+"");

            return mapElement;
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
        throw new IllegalStateException("Not implemented yet");
    }
}
