package scraper.core.template;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import scraper.api.template.T;
import scraper.core.exp.TemplateExpressionVisitor;
import scraper.core.exp.TemplateParser;

import java.util.ArrayList;

public class TemplateBadTest {

    @Test // force wrong amount of children for coverage
    public void badTemplateRootTest() {
        Assertions.assertThrows(AssertionError.class, () -> {
            TemplateParser.TemplateContext ctx = new TemplateParser.TemplateContext(null, 0){
                @Override public String getText() { return ""; }
            };
            ctx.children = new ArrayList<>();
            ctx.children.add(null);
            ctx.children.add(null);
            ctx.children.add(null);
            ctx.children.add(null);
            ctx.children.add(null);
            new TemplateExpressionVisitor<>(new T<String>(){})
                    .visitTemplate(ctx);
                });
    }

    @Test // force wrong amount of children for coverage
    public void badTemplateRoot2Test() {
        Assertions.assertThrows(AssertionError.class, () -> {
            TemplateParser.TemplateContext ctx = new TemplateParser.TemplateContext(null, 0){
                @Override public String getText() { return ""; }
            };
            ctx.children = new ArrayList<>();
            ctx.children.add(null);
            ctx.children.add(null);
            ctx.children.add(null);
            ctx.children.add(null);
            new TemplateExpressionVisitor<>(new T<String>(){})
                    .visitTemplate(ctx);
        });
    }

    @Test // force wrong amount of children for coverage
    public void badFmlLookupTest() {
        Assertions.assertThrows(AssertionError.class, () -> {
        TemplateParser.FmlookupContext ctx = new TemplateParser.FmlookupContext(null, 0){
            @Override public String getText() { return ""; }
        };
        ctx.children = new ArrayList<>();
        ctx.children.add(null);
        new TemplateExpressionVisitor<>(new T<String>(){})
                .visitFmlookup(ctx);
        });
    }

    @Test // force wrong amount of children for coverage
    public void badRootTest() {
            Assertions.assertThrows(AssertionError.class, () -> {
        TemplateParser.RootContext ctx = new TemplateParser.RootContext(null, 0){
            @Override public String getText() { return "1"; }
        };
        ctx.children = new ArrayList<>();
        ctx.children.add(null);
        new TemplateExpressionVisitor<>(new T<String>(){})
                .visitRoot(ctx);
            });
    }

}