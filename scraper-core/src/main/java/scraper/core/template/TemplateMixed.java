package scraper.core.template;

import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import scraper.api.converter.StringToClassConverter;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TemplateMixed<T> extends TemplateExpression<T>{
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TemplateMixed.class);

    private List<Object> concatTemplatesOrStrings = new ArrayList<>();

    public TemplateMixed(TypeToken<T> targetType) {
        super(targetType);
    }

    public T eval(final FlowMap o) {
        try{
            TypeToken<String> stringTypeToken = TypeToken.of(String.class);
            String lookup = "";
            for (Object t : concatTemplatesOrStrings) {
                if (t instanceof TemplateExpression) {
                    Object evaled = ((TemplateExpression) t).eval(o);
                    lookup += String.valueOf(evaled);
                } else if (t instanceof String) {
                    lookup += t;
                } else {
                    throw new TemplateException("Unknown class " + t.getClass().getSimpleName());
                }
            }


            return (T) targetType.getRawType().cast(StringToClassConverter.convert(lookup, targetType.getRawType()));
            // TODO enable dynamic change of converter
        } catch (Exception e) {
            throw new TemplateException("Could not evaluate template string '"+toString()+"'. "+ e.toString());
        }
    }

    public String toString() {
        StringBuilder str = new StringBuilder();

        for (Object concatTemplatesOrString : concatTemplatesOrStrings) {
            str.append(concatTemplatesOrString.toString());
        }

        return str.toString();
    }

    public Collection<String> getKeysInTemplate(FlowMap fm) {
        List<List<String>> ret = concatTemplatesOrStrings.stream().map((Function<Object, List<String>>) o -> {
            if (o instanceof TemplateExpression) {
                Collection<String> t = ((TemplateExpression<?>) o).getKeysInTemplate(fm);
                return new ArrayList<>(t);
            } else {
                return List.of();
            }
        }).collect(Collectors.toList());

        return ret.stream().flatMap(List::stream).collect(Collectors.toList());
    }

    public void addTemplateOrString(Object intermediateTemplate) {
        concatTemplatesOrStrings.add(intermediateTemplate);
    }
}
