// Generated from Template.g4 by ANTLR 4.7.2

    package scraper.core.exp;

import com.google.common.reflect.TypeToken;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import scraper.core.template.*;

import java.util.List;
import java.util.Map;

public class TemplateExpressionVisitor<T> extends AbstractParseTreeVisitor<TemplateExpression<T>> implements TemplateVisitor<TemplateExpression<T>> {

    private final TypeToken<T> target;

    public TemplateExpressionVisitor(TypeToken<T> templ) {
        super();
        this.target = templ;
    }

    @Override
    public TemplateExpression<T> visitRoot(TemplateParser.RootContext ctx) {
        if(ctx.children.size() != 2) { throw new RuntimeException("Root wrong children size: " + ctx.children.size()); }
        return visit(ctx.children.get(0));
    }

    @Override public TemplateExpression<T> visitTemplate(TemplateParser.TemplateContext ctx) {
        if(ctx.children.size() != 1) { throw new RuntimeException("Template has more than 1 child"); }
        return visit(ctx.children.get(0));
    }

    @Override
    public TemplateExpression<T> visitMixedtemplate(TemplateParser.MixedtemplateContext ctx) {
        TemplateMixed<T> result = new TemplateMixed<>(target);

        if(ctx.children != null) {
            TemplateExpressionVisitor<String> targetVisitor = new TemplateExpressionVisitor<>(TypeToken.of(String.class));
            for (ParseTree child : ctx.children) {
                TemplateExpression<String> visited = targetVisitor.visit(child);
                if(visited != null) result.addTemplateOrString(visited);
            }
        }

        return result;
    }

    @Override
    public TemplateExpression<T> visitFmlookup(TemplateParser.FmlookupContext ctx) {
        if(ctx.children.size() != 3) throw new RuntimeException("FM Lookup Template does not look like ( T )");

        TemplateExpressionVisitor<String> stringTargetVisitor = new TemplateExpressionVisitor<>(TypeToken.of(String.class));
        TemplateExpression<String> visited = stringTargetVisitor.visit(ctx.children.get(1));

        return new TemplateMapKey<>(visited, target);
    }

    @Override
    public TemplateExpression<T> visitMllookup(TemplateParser.MllookupContext ctx) {
        ParseTree listexp = ctx.getChild(0);
        ParseTree intexp = ctx.getChild(2);

        TypeToken<List<T>> listToken = new TypeToken<>(){};
        TemplateExpressionVisitor<List<T>> listTargetVisitor = new TemplateExpressionVisitor<>(listToken);
        TemplateExpressionVisitor<Integer> intTargetVisitor = new TemplateExpressionVisitor<>(TypeToken.of(Integer.class));
        TemplateExpression<List<T>> templateList = listTargetVisitor.visit(listexp);
        TemplateExpression<Integer> templateInt = intTargetVisitor.visit(intexp);

        TypeToken<Map<String, T>> mapToken = new TypeToken<>(){};
        TemplateExpressionVisitor<Map<String, T>> mapTargetVisitor = new TemplateExpressionVisitor<>(mapToken);
        TemplateExpressionVisitor<String> stringTargetVisitor = new TemplateExpressionVisitor<>(TypeToken.of(String.class));
        TemplateExpression<Map<String, T>> templateMap = mapTargetVisitor.visit(listexp);
        TemplateExpression<String> templateString = stringTargetVisitor.visit(intexp);

        return new TemplateMapOrListLookup<>(templateMap, templateList, templateInt, templateString, target);
    }

    @Override
    public TemplateExpression<T> visitStringcontent(TemplateParser.StringcontentContext ctx) {
        TemplateString<T> content = new TemplateString<>(target);
        String unescape = ctx.getText()
                .replaceAll("\\\\\\{", "{")
                .replaceAll("\\\\}", "}")
                .replaceAll("\\\\\\[", "[")
                .replaceAll("\\\\]", "]")
                ;
        content.addContent(unescape);
        return content;
    }

    @Override
    public TemplateExpression<T> visitAppend(TemplateParser.AppendContext ctx) {
        throw new IllegalStateException("Append not implemented yet");
    }

}