// Generated from Template.g4 by ANTLR 4.7.2

    package scraper.core.exp;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraper.api.exceptions.TemplateException;
import scraper.api.template.T;
import scraper.api.template.Term;
import scraper.api.template.TypeReplacer;
import scraper.core.template.*;

import java.lang.reflect.TypeVariable;
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
            return new TemplateConstant<>("", target);
        }

        if(ctx.children.size() != 2) throw new AssertionError("Bad root definition");
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
            if(
                    !TypeToken.of(target.get()).isSupertypeOf(TypeToken.of(String.class))
                    && !(target.get() instanceof TypeVariable)
            )
                throw new RuntimeException("Mixed template target has to be supertype of java.lang.String or a Type variable");

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
                ParseTree listexp = ctx.getChild(1);
                ParseTree intexp = ctx.getChild(3);
                @SuppressWarnings("unchecked") // type parameter lost with target.get() but fully reconstructed
                TypeToken<List<Y>> listToken = listOf((TypeToken<Y>) TypeToken.of(target.get()));
                TemplateExpressionVisitor<List<Y>> listTargetVisitor = new TemplateExpressionVisitor<>(new T<>(listToken.getType()){});
                TemplateExpressionVisitor<Integer> intTargetVisitor = new TemplateExpressionVisitor<>(new T<>(){});
                TemplateExpression<List<Y>> templateList = listTargetVisitor.visit(listexp);
                TemplateExpression<Integer> templateInt = intTargetVisitor.visit(intexp);


                return new TemplateListLookup<>(templateList, templateInt, target);
            }

            if (ctx.getChild(2) instanceof TemplateParser.MaplookupContext) {
                boolean fromTypeVar = target.get() instanceof TypeVariable;

                ParseTree mapexp = ctx.getChild(1);
                ParseTree strexp = ctx.getChild(2);
                @SuppressWarnings("unchecked") // type parameter lost with target.get() but fully reconstructed
                TypeToken<Map<String, Y>> mapToken = mapOf(TypeToken.of(String.class), (TypeToken<Y>) TypeToken.of(target.get()));

                // replace Y with Y$MapOf if type variable
                T<Map<String, Y>> token = new T<>(mapToken.getType()){};
                if(fromTypeVar) token = new T<>(new TypeReplacer("MapOf"){}.visit(token.get())){};

                TemplateExpressionVisitor<Map<String, Y>> mapTargetVisitor = new TemplateExpressionVisitor<>(token){};
                TemplateExpressionVisitor<String> stringTargetVisitor = new TemplateExpressionVisitor<>(new T<>(){});
                TemplateExpression<Map<String, Y>> templateMap = mapTargetVisitor.visit(mapexp);
                TemplateExpression<String> templateString = stringTargetVisitor.visit(strexp);

                return new TemplateMapLookup<>(
                        // TODO why cannot this be inferred? withTypeVar just returns itself
                        //  templateMap.withTypeVar(),
                        (fromTypeVar ? (TemplateExpression<Map<String, Y>>) templateMap.withTypeVar("MapLookup") : templateMap),
                        templateString,
                        new T<>(target.get(), (fromTypeVar ? target.getSuffix() + "MapLookup" : target.getSuffix())){},
                        fromTypeVar
                );
//                return new TemplateMapLookup<>(templateMap, templateString, target, target.get() instanceof TypeVariable);
            }
        }


        throw new AssertionError("Template has wrong amount of children: " + ctx.getText());
    }

    @Override
    public TemplateExpression<Y> visitFmlookup(TemplateParser.FmlookupContext ctx) {
        l.trace("Visiting fm lookup '{}'", ctx.getText());
        if(ctx.children.size() != 3) throw new AssertionError("Bad FmlLookup");

        TemplateExpressionVisitor<String> stringTargetVisitor = new TemplateExpressionVisitor<>(new T<>(){});
        TemplateExpression<String> visited = stringTargetVisitor.visit(ctx.children.get(1));

        return new TemplateMapKey<>(visited, target, "", (target.get() instanceof TypeVariable));
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
        l.trace("String content '{}'", ctx.getText());
        String unescape = ctx.getText()
                .replaceAll("\\\\\\{", "{")
                .replaceAll("\\\\}", "}")
                .replaceAll("\\\\\\[", "[")
                .replaceAll("\\\\]", "]")
                ;
        // TODO check if Y is String else error
        TemplateConstant<Y> content = new TemplateConstant<>((Y) unescape, target);
//        content.eval();
        return content;
    }

}