package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.reflect.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TemplateMixed extends TemplateExpression<String> implements Concatenation<String> {
    @Override public void accept(TVisitor visitor) { visitor.visitConcatenation(this); }
    private List<Term<String>> concatTemplatesOrStrings = new ArrayList<>();

    @Override
    public List<Term<String>> getConcatTemplatesOrStrings() {
        return concatTemplatesOrStrings;
    }

    public TemplateMixed() { super(new T<>(String.class){}); }

    public String eval(@NotNull final FlowMap o) {
        try{
            StringBuilder lookup = new StringBuilder();
            for (Term<String> term : concatTemplatesOrStrings) {
                String toConcat = term.eval(o);
                lookup.append(toConcat);
            }

            return lookup.toString();
        } catch (Exception e) {
            throw new TemplateException("Could not evaluate template string '"+toString()+"'. "+ e.toString());
        }
    }

    @Override
    public Object getRaw() {
        return toString();
    }

    public String toString() {
        StringBuilder str = new StringBuilder();

        for (Term<String> concatTemplatesOrString : concatTemplatesOrStrings) {
            str.append(concatTemplatesOrString.getRaw());
        }

        return str.toString();
    }

    @NotNull
    public Map<String, T<?>> getKeysInTemplate(@NotNull FlowMap fm) {
        Map<String, T<?>> allKeys = new HashMap<>();

        List<Map<String, T<?>>> ret = concatTemplatesOrStrings.stream().map((Function<Object, Map<String, T<?>>>) o -> {
            if (o instanceof TemplateExpression) {
                Map<String, T<?>> t = ((TemplateExpression<?>) o).getKeysInTemplate(fm);
                return new HashMap<>(t);
            } else {
                return Map.of();
            }
        }).collect(Collectors.toList());

        ret.forEach(m -> m.forEach((key, type) -> {
                    if(allKeys.containsKey(key) && !allKeys.get(key).equals(type))
                        throw new IllegalStateException("Types don't match");
                    allKeys.put(key,type);
        }
                ));

        return allKeys;
    }

    public void addTemplateOrString(Term<String> intermediateTemplate) {
        concatTemplatesOrStrings.add(intermediateTemplate);
    }

}
