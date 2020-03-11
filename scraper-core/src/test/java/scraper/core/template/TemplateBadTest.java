package scraper.core.template;

import com.google.common.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.template.T;
import scraper.api.template.Term;
import scraper.core.exp.TemplateExpressionVisitor;
import scraper.core.exp.TemplateParser;
import scraper.util.TemplateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TemplateBadTest {

    @Test(expected = AssertionError.class) // force wrong amount of children for coverage
    public void badTemplateRootTest() {
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
    }

    @Test(expected = AssertionError.class) // force wrong amount of children for coverage
    public void badTemplateRoot2Test() {
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
    }

    @Test(expected = AssertionError.class) // force wrong amount of children for coverage
    public void badFmlLookupTest() {
        TemplateParser.FmlookupContext ctx = new TemplateParser.FmlookupContext(null, 0){
            @Override public String getText() { return ""; }
        };
        ctx.children = new ArrayList<>();
        ctx.children.add(null);
        new TemplateExpressionVisitor<>(new T<String>(){})
                .visitFmlookup(ctx);
    }

    @Test(expected = AssertionError.class) // force wrong amount of children for coverage
    public void badRootTest() {
        TemplateParser.RootContext ctx = new TemplateParser.RootContext(null, 0){
            @Override public String getText() { return "1"; }
        };
        ctx.children = new ArrayList<>();
        ctx.children.add(null);
        new TemplateExpressionVisitor<>(new T<String>(){})
                .visitRoot(ctx);
    }

}