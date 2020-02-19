// Generated from Template.g4 by ANTLR 4.7.2

    package scraper.core.exp;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraper.core.template.*;

import java.util.List;
import java.util.Map;

public class TemplateExpressionVisitor<T> extends AbstractParseTreeVisitor<TemplateExpression<T>> implements TemplateVisitor<TemplateExpression<T>> {

    protected Logger l = LoggerFactory.getLogger(getClass());

    private final TypeToken<T> target;

    public TemplateExpressionVisitor(TypeToken<T> templ) {
        super();
        this.target = templ;
    }

    @Override
    public TemplateExpression<T> visitRoot(TemplateParser.RootContext ctx) {
        l.trace("Parsing template '{}'", ctx.getText());
        if(ctx.getText().isEmpty()) {
            TemplateString<T> t = new TemplateString<>(target);
            t.addContent("");
            return t;
        }

        if(ctx.children.size() != 2) { throw new RuntimeException("Root wrong children size: " + ctx.children.size()); }
        return visit(ctx.children.get(0));
    }

    @Override
    public TemplateExpression<T> visitTemplate(TemplateParser.TemplateContext ctx) {
        l.trace("Visiting template '{}'", ctx.getText());
        // string content or fmlookup
        if(ctx.children.size() == 1) {
            return visit(ctx.getChild(0));
        }

        // template template
        if(ctx.children.size() == 2) {
            TemplateMixed<T> result = new TemplateMixed<>(target);

            TemplateExpressionVisitor<String> targetVisitor = new TemplateExpressionVisitor<>(TypeToken.of(String.class));

            TemplateExpression<String> visited = targetVisitor.visit(ctx.getChild(0));
            result.addTemplateOrString(visited);

            visited = targetVisitor.visit(ctx.getChild(1));
            result.addTemplateOrString(visited);

            return result;
        }


        // lookup or append
        if (ctx.children.size() == 4) {

            if (ctx.getChild(3) instanceof TemplateParser.AppendContext) {
                l.trace("Append");
                throw new RuntimeException("Append not implemented");
            }

            if (ctx.getChild(3) instanceof TemplateParser.ArraymaplookupContext) {
                l.trace("Array or map lookup");
                ParseTree listexp = ctx.getChild(1);
                ParseTree intexp = ctx.getChild(3);

                TypeToken<List<T>> listToken = listOf(target);
                TemplateExpressionVisitor<List<T>> listTargetVisitor = new TemplateExpressionVisitor<>(listToken);
                TemplateExpressionVisitor<Integer> intTargetVisitor = new TemplateExpressionVisitor<>(TypeToken.of(Integer.class));
                TemplateExpression<List<T>> templateList = listTargetVisitor.visit(listexp);
                TemplateExpression<Integer> templateInt = intTargetVisitor.visit(intexp);

                TypeToken<Map<String, T>> mapToken = mapOf(TypeToken.of(String.class), target);
                TemplateExpressionVisitor<Map<String, T>> mapTargetVisitor = new TemplateExpressionVisitor<>(mapToken);
                TemplateExpressionVisitor<String> stringTargetVisitor = new TemplateExpressionVisitor<>(TypeToken.of(String.class));
                TemplateExpression<Map<String, T>> templateMap = mapTargetVisitor.visit(listexp);
                TemplateExpression<String> templateString = stringTargetVisitor.visit(intexp);

                return new TemplateMapOrListLookup<>(templateMap, templateList, templateInt, templateString, target);
            }
        }


        throw new RuntimeException("Template has wrong amount of children: " + ctx.getText());
    }

    @Override
    public TemplateExpression<T> visitFmlookup(TemplateParser.FmlookupContext ctx) {
        l.trace("Visiting fm lookup '{}'", ctx.getText());
        if(ctx.children.size() != 3) throw new RuntimeException("FM Lookup Template does not look like ( T )");

        TemplateExpressionVisitor<String> stringTargetVisitor = new TemplateExpressionVisitor<>(TypeToken.of(String.class));
        TemplateExpression<String> visited = stringTargetVisitor.visit(ctx.children.get(1));

        return new TemplateMapKey<>(visited, target);
    }

    @Override
    public TemplateExpression<T> visitArraymaplookup(TemplateParser.ArraymaplookupContext ctx) {
        return visit(ctx.getChild(1));
    }

    @Override
    public TemplateExpression<T> visitAppend(TemplateParser.AppendContext ctx) {
        l.trace("Visiting append '{}'", ctx.getText());
        return null;
    }

    @Override
    public TemplateExpression<T> visitStringcontent(TemplateParser.StringcontentContext ctx) {
        l.trace("String content '{}'", ctx.getText());
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

    static <K, V> TypeToken<Map<K, V>> mapOf(TypeToken<K> keyType, TypeToken<V> valueType) {
        return new TypeToken<Map<K, V>>() {}
                .where(new TypeParameter<>() {}, keyType)
                .where(new TypeParameter<>() {}, valueType);
    }

    static <K> TypeToken<List<K>> listOf(TypeToken<K> elementType) {
        return new TypeToken<List<K>>() {}.where(new TypeParameter<>() {}, elementType);
    }
}