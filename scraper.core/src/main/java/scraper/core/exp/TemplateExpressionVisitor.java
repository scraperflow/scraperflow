package scraper.core.exp;

import scraper.api.T;
import scraper.api.Term;
import scraper.api.TypeReplacer;
import scraper.core.template.*;

import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

import static java.lang.System.Logger.Level.TRACE;
import static scraper.util.TemplateUtil.listOf;
import static scraper.util.TemplateUtil.mapOf;

public class TemplateExpressionVisitor<Y> implements TemplateVisitor<TemplateExpression<Y>> {

    protected System.Logger log = System.getLogger("TemplateParser");

    private final T<Y> target;

    public TemplateExpressionVisitor(T<Y> templ) {
        super();
        this.target = templ;
    }

//    @Override
//    public TemplateExpression<Y> visitTemplate(Ast.TemplateNode ctx) {
//        log.log(TRACE,    "Visiting template {0}", ctx);
//
//
//        return null;
//        // string content or fmlookup or fmlookupconsume
//        if(ctx.children.size() == 1) {
//            return visit(ctx.getChild(0));
//        }
//
//        // template template
//        if(ctx.children.size() == 2) {
//            if(
//                    !(target.equalsType(new T<String>(){}))
//                    && !(target.get() instanceof TypeVariable)
//            )
//                throw new RuntimeException("Mixed template target has to be type of java.lang.String or a Type variable");
//
//            TemplateMixed result = new TemplateMixed();
//
//            TemplateExpressionVisitor<String> targetVisitor = new TemplateExpressionVisitor<>(new T<>(){});
//
//            Term<String> visited = targetVisitor.visit(ctx.getChild(0));
//            result.addTemplateOrString(visited);
//
//            visited = targetVisitor.visit(ctx.getChild(1));
//            result.addTemplateOrString(visited);
//
//            @SuppressWarnings({"UnnecessaryLocalVariable", "unchecked"}) // cast checked with token
//            TemplateExpression<Y> castResult = (TemplateExpression<Y>) result;
//            return castResult;
//    }

    @Override
    public TemplateExpression<Y> visitMixed(Ast.MixedNode ctx) {
        TemplateMixed result = new TemplateMixed();

        TemplateExpressionVisitor<String> targetVisitor = new TemplateExpressionVisitor<>(new T<>(){});

        Term<String> visited = targetVisitor.visitTemplate(ctx.t1);
        result.addTemplateOrString(visited);

        visited = targetVisitor.visitTemplate(ctx.t2);
        result.addTemplateOrString(visited);

        @SuppressWarnings({"UnnecessaryLocalVariable", "unchecked"}) // cast checked with token
        TemplateExpression<Y> castResult = (TemplateExpression<Y>) result;
        return castResult;
    }

    @Override
    public TemplateExpression<Y> visitFmlookupconsume(Ast.FmLookupConsumeNode ctx) {
        log.log(TRACE,    "Visiting fm lookup consume {0}", ctx);

        TemplateExpressionVisitor<String> stringTargetVisitor = new TemplateExpressionVisitor<>(new T<>(){});
        TemplateExpression<String> visited = stringTargetVisitor.visitTemplate(ctx.template);

        return new TemplateMapKey<>(visited, target, (target.get() instanceof TypeVariable), true);
    }

    @Override
    public TemplateExpression<Y> visitFmlookup(Ast.FmLookupNode ctx) {
        log.log(TRACE,    "Visiting fm lookup {0}", ctx);

        TemplateExpressionVisitor<String> stringTargetVisitor = new TemplateExpressionVisitor<>(new T<>(){});
        TemplateExpression<String> visited = stringTargetVisitor.visitTemplate(ctx.template);

        return new TemplateMapKey<>(visited, target, (target.get() instanceof TypeVariable));
    }

    @Override
    public TemplateExpression<Y> visitArraylookup(Ast.ListIndexNode ctx) {
        boolean fromTypeVar = target.get() instanceof TypeVariable;

        Ast.TemplateNode listexp = ctx.list;
        Ast.TemplateNode intexp = ctx.index;
        T<List<Y>> listToken = listOf(target);

        // replace Y with Y$MapOf if type variable
        if(fromTypeVar) listToken = new T<>(new TypeReplacer(target.getTypeString()){}.visit(listToken.get())){};

        TemplateExpressionVisitor<List<Y>> listTargetVisitor = new TemplateExpressionVisitor<>(listToken);
        TemplateExpressionVisitor<Integer> intTargetVisitor = new TemplateExpressionVisitor<>(new T<>(){});
        TemplateExpression<List<Y>> templateList = listTargetVisitor.visitTemplate(listexp);
        TemplateExpression<Integer> templateInt = intTargetVisitor.visitTemplate(intexp);

        return new TemplateListLookup<>(templateList, templateInt, target);
    }

    @Override
    public TemplateExpression<Y> visitMaplookup(Ast.MapKeyNode ctx) {
        boolean fromTypeVar = target.get() instanceof TypeVariable;

        Ast.TemplateNode mapexp = ctx.map;
        Ast.TemplateNode strexp = ctx.key;
        T<Map<String, Y>> token = mapOf(new T<>(){}, target);

        // replace Y with Y$MapOf if type variable
        if (fromTypeVar) token = new T<>(new TypeReplacer(target.getTypeString()){}.visit(token.get())){};

        TemplateExpressionVisitor<Map<String, Y>> mapTargetVisitor = new TemplateExpressionVisitor<>(token) {};
        TemplateExpressionVisitor<String> stringTargetVisitor = new TemplateExpressionVisitor<>(new T<>() {});

        TemplateExpression<Map<String, Y>> templateMap = mapTargetVisitor.visitTemplate(mapexp);
        TemplateExpression<String> templateString = stringTargetVisitor.visitTemplate(strexp);

        return new TemplateMapLookup<>(
                templateMap,
                templateString,
                new T<>(target.get(), (fromTypeVar ? target.getSuffix() + "MapLookup" : target.getSuffix())) {},
                fromTypeVar
        );
    }

    @Override
    public TemplateExpression<Y> visitStringcontent(Ast.StringNode ctx) {
        log.log(TRACE,    "String content {0}", ctx);
        String unescape = ctx.toString()
                .replaceAll("\\\\\\{", "{")
                .replaceAll("\\\\}", "}")
                .replaceAll("\\\\\\^", "^")
                .replaceAll("\\\\@", "@")
                .replaceAll("\\\\\\[", "[")
                .replaceAll("\\\\]", "]")
                ;
        return new TemplateConstant<>(unescape, target);
    }


//    @Override
//    public TemplateExpression<Y> visitTemplate(TemplateParser.TemplateContext ctx) {
//
//
//        // lookup or append
//        if (ctx.children.size() == 4) {
//            if (ctx.getChild(3) instanceof TemplateParser.ArraylookupContext) {
//            }
//        }
//
//
//        throw new AssertionError("Template has wrong amount of children: " + ctx.getText());
//    }
//
//    @Override
//    public TemplateExpression<Y> visitFmlookupconsume(TemplateParser.FmlookupconsumeContext ctx) {
//    }
//
//    @Override
//    public TemplateExpression<Y> visitArraylookup(TemplateParser.ArraylookupContext ctx) {
//        try {
//            return visit(ctx.getChild(1));
//        } catch (Exception e){
//            throw new TemplateException(e, "Bad array lookup template: " + ctx.getText());
//        }
//    }
//
//    @Override
//    public TemplateExpression<Y> visitMaplookup(TemplateParser.MaplookupContext ctx) {
//        try {
//            return visit(ctx.getChild(1));
//        } catch (Exception e){
//            throw new TemplateException(e, "Bad map lookup template" + ctx.getText());
//        }
//    }

}