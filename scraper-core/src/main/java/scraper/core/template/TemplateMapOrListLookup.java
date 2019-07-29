package scraper.core.template;

import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TemplateMapOrListLookup<T> extends TemplateExpression<T> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TemplateMapOrListLookup.class);

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


    public T eval(final FlowMap o) {
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
            throw new TemplateException("Could not evaluate array/map lookup template '"+toString()+"'. ");
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append(list.toString()+"["+index.toString()+"]");

        return str.toString();
    }

    public Collection<String> getKeysInTemplate(FlowMap o) {
        return null;
    }
}
