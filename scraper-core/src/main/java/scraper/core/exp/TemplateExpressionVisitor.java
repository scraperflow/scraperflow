// Generated from Template.g4 by ANTLR 4.7.2

    package scraper.core.exp;

import com.google.common.reflect.TypeToken;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraper.api.reflect.T;
import scraper.api.reflect.Term;
import scraper.core.template.*;
import scraper.util.TemplateUtil;

import java.util.List;
import java.util.Map;

import static scraper.util.TemplateUtil.listOf;
import static scraper.util.TemplateUtil.mapOf;

public class TemplateExpressionVisitor<Y> extends AbstractParseTreeVisitor<TemplateExpression<Y>> implements TemplateVisitor<TemplateExpression<Y>> {

    protected Logger l = LoggerFactory.getLogger(getClass());

    private final T<Y> target;

    public TemplateExpressionVisitor(T<Y> templ) {
        super();
        this.target = templ;
    }

    @Override
    public TemplateExpression<Y> visitRoot(TemplateParser.RootContext ctx) {
        l.trace("Parsing template '{}'", ctx.getText());
        if(ctx.getText().isEmpty()) {
            TemplateString<Y> t = new TemplateString<>(target);
            t.addContent("");
            return t;
        }

        if(ctx.children.size() != 2) { throw new RuntimeException("Root wrong children size: " + ctx.children.size()); }
        return visit(ctx.children.get(0));
    }

    @Override
    public TemplateExpression<Y> visitTemplate(TemplateParser.TemplateContext ctx) {
        l.trace("Visiting template '{}'", ctx.getText());
        // string content or fmlookup
        if(ctx.children.size() == 1) {
            return visit(ctx.getChild(0));
        }

        // template template
        if(ctx.children.size() == 2) {
            if(!TypeToken.of(target.get()).isSupertypeOf(TypeToken.of(String.class)))
                throw new RuntimeException("Mixed template target has to be supertype of java.lang.String");

            TemplateMixed result = new TemplateMixed();

            TemplateExpressionVisitor<String> targetVisitor = new TemplateExpressionVisitor<>(new T<>(){});

            Term<String> visited = targetVisitor.visit(ctx.getChild(0));
            result.addTemplateOrString(visited);

            visited = targetVisitor.visit(ctx.getChild(1));
            result.addTemplateOrString(visited);

            // TODO cast correct?
            return (TemplateExpression<Y>) result;
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
                @SuppressWarnings("unchecked") // generics type parameter is lost with target.get() but fully reconstructed
                TypeToken<List<? extends Y>> listToken = TemplateUtil.listOf((TypeToken<Y>) TypeToken.of(target.get()));
                TemplateExpressionVisitor<List<? extends Y>> listTargetVisitor = new TemplateExpressionVisitor<>(new T<>(listToken.getType()){});
                TemplateExpressionVisitor<Integer> intTargetVisitor = new TemplateExpressionVisitor<>(new T<>(){});
                TemplateExpression<List<? extends Y>> templateList = listTargetVisitor.visit(listexp);
                TemplateExpression<Integer> templateInt = intTargetVisitor.visit(intexp);

                @SuppressWarnings("unchecked") // generics type parameter is lost with target.get() but fully reconstructed
                TypeToken<Map<String, ? extends Y>> mapToken = mapOf(TypeToken.of(String.class), (TypeToken<Y>) TypeToken.of(target.get()));
                TemplateExpressionVisitor<Map<String, ? extends Y>> mapTargetVisitor = new TemplateExpressionVisitor<>(new T<>(mapToken.getType()){});
                TemplateExpressionVisitor<String> stringTargetVisitor = new TemplateExpressionVisitor<>(new T<>(){});
                TemplateExpression<Map<String, ? extends Y>> templateMap = mapTargetVisitor.visit(listexp);
                TemplateExpression<String> templateString = stringTargetVisitor.visit(intexp);

                return new TemplateMapOrListLookup<>(templateMap, templateList, templateInt, templateString, target);
            }
        }


        throw new RuntimeException("Template has wrong amount of children: " + ctx.getText());
    }

    @Override
    public TemplateExpression<Y> visitFmlookup(TemplateParser.FmlookupContext ctx) {
        l.trace("Visiting fm lookup '{}'", ctx.getText());
        if(ctx.children.size() != 3) throw new RuntimeException("FM Lookup Template does not look like { T }");

        TemplateExpressionVisitor<String> stringTargetVisitor = new TemplateExpressionVisitor<>(new T<>(){});
        TemplateExpression<String> visited = stringTargetVisitor.visit(ctx.children.get(1));

        return new TemplateMapKey<>(visited, target);
    }

    @Override
    public TemplateExpression<Y> visitArraymaplookup(TemplateParser.ArraymaplookupContext ctx) {
        return visit(ctx.getChild(1));
    }

    @Override
    public TemplateExpression<Y> visitAppend(TemplateParser.AppendContext ctx) {
        l.trace("Visiting append '{}'", ctx.getText());
        return null;
    }

    @Override
    public TemplateExpression<Y> visitStringcontent(TemplateParser.StringcontentContext ctx) {
        l.trace("String content '{}'", ctx.getText());
        TemplateString<Y> content = new TemplateString<>(target);
        String unescape = ctx.getText()
                .replaceAll("\\\\\\{", "{")
                .replaceAll("\\\\}", "}")
                .replaceAll("\\\\\\[", "[")
                .replaceAll("\\\\]", "]")
                ;
        content.addContent(unescape);
        return content;
    }

}