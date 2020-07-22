// Generated from Template.g4 by ANTLR 4.7.2

    package scraper.core.exp;

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import scraper.api.exceptions.TemplateException;
import scraper.api.template.T;
import scraper.api.template.Term;
import scraper.api.template.TypeReplacer;
import scraper.core.template.*;

import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

import static java.lang.System.Logger.Level.TRACE;
import static scraper.util.TemplateUtil.listOf;
import static scraper.util.TemplateUtil.mapOf;

public class TemplateExpressionVisitor<Y> extends AbstractParseTreeVisitor<TemplateExpression<Y>> implements TemplateVisitor<TemplateExpression<Y>> {

    protected System.Logger log = System.getLogger("TemplateParser");

    private final T<Y> target;

    public TemplateExpressionVisitor(T<Y> templ) {
        super();
        this.target = templ;
    }

    @Override
    public TemplateExpression<Y> visitRoot(TemplateParser.RootContext ctx) {
        log.log(TRACE,    "Parsing template {0}", ctx.getText());
        if(ctx.getText().isEmpty()) {
            return new TemplateConstant<>("", target);
        }

        if(ctx.children.size() != 2) throw new AssertionError("Bad root definition");
        return visit(ctx.children.get(0));
    }

    @Override
    public TemplateExpression<Y> visitTemplate(TemplateParser.TemplateContext ctx) {
        log.log(TRACE,    "Visiting template {0}", ctx.getText());

        // string content or fmlookup
        if(ctx.children.size() == 1) {
            return visit(ctx.getChild(0));
        }

        // template template
        if(ctx.children.size() == 2) {
            if(
                    !(target.equalsType(new T<String>(){}))
                    && !(target.get() instanceof TypeVariable)
            )
                throw new RuntimeException("Mixed template target has to be type of java.lang.String or a Type variable");

            TemplateMixed result = new TemplateMixed();

            TemplateExpressionVisitor<String> targetVisitor = new TemplateExpressionVisitor<>(new T<>(){});

            Term<String> visited = targetVisitor.visit(ctx.getChild(0));
            result.addTemplateOrString(visited);

            visited = targetVisitor.visit(ctx.getChild(1));
            result.addTemplateOrString(visited);

            @SuppressWarnings({"UnnecessaryLocalVariable", "unchecked"}) // cast checked with token
            TemplateExpression<Y> castResult = (TemplateExpression<Y>) result;
            return castResult;
        }


        // lookup or append
        if (ctx.children.size() == 4) {
            if (ctx.getChild(3) instanceof TemplateParser.ArraylookupContext) {
                boolean fromTypeVar = target.get() instanceof TypeVariable;

                ParseTree listexp = ctx.getChild(1);
                ParseTree intexp = ctx.getChild(3);
                T<List<Y>> listToken = listOf(target);

                // replace Y with Y$MapOf if type variable
                if(fromTypeVar) listToken = new T<>(new TypeReplacer("ListOf"){}.visit(listToken.get())){};

                TemplateExpressionVisitor<List<Y>> listTargetVisitor = new TemplateExpressionVisitor<>(listToken);
                TemplateExpressionVisitor<Integer> intTargetVisitor = new TemplateExpressionVisitor<>(new T<>(){});
                TemplateExpression<List<Y>> templateList = listTargetVisitor.visit(listexp);
                TemplateExpression<Integer> templateInt = intTargetVisitor.visit(intexp);


                return new TemplateListLookup<>(templateList, templateInt, target);
            }

            if (ctx.getChild(2) instanceof TemplateParser.MaplookupContext) {
                boolean fromTypeVar = target.get() instanceof TypeVariable;

                ParseTree mapexp = ctx.getChild(1);
                ParseTree strexp = ctx.getChild(2);
                T<Map<String, Y>> token = mapOf(new T<>(){}, target);

                // replace Y with Y$MapOf if type variable
                if(fromTypeVar) token = new T<>(new TypeReplacer("MapOf"){}.visit(token.get())){};

                TemplateExpressionVisitor<Map<String, Y>> mapTargetVisitor = new TemplateExpressionVisitor<>(token){};
                TemplateExpressionVisitor<String> stringTargetVisitor = new TemplateExpressionVisitor<>(new T<>(){});
                TemplateExpression<Map<String, Y>> templateMap = mapTargetVisitor.visit(mapexp);
                TemplateExpression<String> templateString = stringTargetVisitor.visit(strexp);

                return new TemplateMapLookup<>(
                        templateMap,
                        templateString,
                        new T<>(target.get(), (fromTypeVar ? target.getSuffix() + "MapLookup" : target.getSuffix())){},
                        fromTypeVar
                );
            }
        }


        throw new AssertionError("Template has wrong amount of children: " + ctx.getText());
    }

    @Override
    public TemplateExpression<Y> visitFmlookup(TemplateParser.FmlookupContext ctx) {
        log.log(TRACE,    "Visiting fm lookup {0}", ctx.getText());
        if(ctx.children.size() != 3) throw new AssertionError("Bad FmlLookup");

        TemplateExpressionVisitor<String> stringTargetVisitor = new TemplateExpressionVisitor<>(new T<>(){});
        TemplateExpression<String> visited = stringTargetVisitor.visit(ctx.children.get(1));

        return new TemplateMapKey<>(visited, target, (target.get() instanceof TypeVariable));
    }

    @Override
    public TemplateExpression<Y> visitArraylookup(TemplateParser.ArraylookupContext ctx) {
        try {
            return visit(ctx.getChild(1));
        } catch (Exception e){
            throw new TemplateException(e, "Bad array lookup template: " + ctx.getText());
        }
    }

    @Override
    public TemplateExpression<Y> visitMaplookup(TemplateParser.MaplookupContext ctx) {
        try {
            return visit(ctx.getChild(1));
        } catch (Exception e){
            throw new TemplateException(e, "Bad map lookup template" + ctx.getText());
        }
    }

    @Override
    public TemplateExpression<Y> visitStringcontent(TemplateParser.StringcontentContext ctx) {
        log.log(TRACE,    "String content {0}", ctx.getText());
        String unescape = ctx.getText()
                .replaceAll("\\\\\\{", "{")
                .replaceAll("\\\\}", "}")
                .replaceAll("\\\\\\^", "^")
                .replaceAll("\\\\@", "@")
                .replaceAll("\\\\\\[", "[")
                .replaceAll("\\\\]", "]")
                ;
        return new TemplateConstant<>(unescape, target);
    }

}