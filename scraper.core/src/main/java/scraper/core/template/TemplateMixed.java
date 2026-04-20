package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.TemplateException;
import scraper.api.FlowMap;
import scraper.api.Concatenation;
import scraper.api.T;
import scraper.api.TVisitor;
import scraper.api.Term;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static scraper.core.converter.StringToClassConverter.convert;

public class TemplateMixed extends TemplateExpression<String> implements Concatenation {
    @Override public <X> X accept(@NotNull TVisitor<X> visitor) { return visitor.visitConcatenation(this); }
    private final List<Term<String>> concatTemplatesOrStrings = new ArrayList<>();

    @NotNull
    @Override
    public List<Term<String>> getConcatenationTerms() {
        return concatTemplatesOrStrings;
    }

    public TemplateMixed() { super(new T<>(String.class){}); }


    @Override
    public String getTypeString() {
        return "String";
    }

    @Override
    public boolean isTypeVariable() {
        return false;
    }

    public String eval(@NotNull final FlowMap o) {
        try{
            StringBuilder lookup = new StringBuilder();
            for (Term<String> term : concatTemplatesOrStrings) {
                String toConcat = (String) convert(term.eval(o), targetType.getRawType());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateMixed that = (TemplateMixed) o;
        return concatTemplatesOrStrings.equals(that.concatTemplatesOrStrings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(concatTemplatesOrStrings);
    }
}
