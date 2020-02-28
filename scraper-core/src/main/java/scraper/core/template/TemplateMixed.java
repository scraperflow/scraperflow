package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.template.Concatenation;
import scraper.api.template.T;
import scraper.api.template.TVisitor;
import scraper.api.template.Term;

import java.util.ArrayList;
import java.util.List;

public class TemplateMixed extends TemplateExpression<String> implements Concatenation {
    @Override public void accept(@NotNull TVisitor visitor) { visitor.visitConcatenation(this); }
    private List<Term<String>> concatTemplatesOrStrings = new ArrayList<>();

    @NotNull
    @Override
    public List<Term<String>> getConcatenationTerms() {
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

    @NotNull
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

    public void addTemplateOrString(Term<String> intermediateTemplate) {
        concatTemplatesOrStrings.add(intermediateTemplate);
    }

}
