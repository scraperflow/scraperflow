package scraper.core.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.units.qual.K;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.util.NodeUtil;
import scraper.util.TemplateUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TemplateExpTest {

    private final static FlowMap o = new FlowMapImpl();
    private final static ObjectMapper mapper = new ObjectMapper();

    @Before
    public void clean() { o.clear(); }

    // ===============
    // SIMPLE TEMPLATE
    // ===============

    // == Eval

    @Test // "hello world"
    public void simpleTemplateNoKeys() {
        String source = "hello world";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        String target = test.eval(o);
        Assert.assertEquals("hello world", target);
    }

    @Test // "{hello} world"
    public void simpleTemplateKeyAndString() {
        String source = "{hello} world";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.put("hello", "ok");
        String target = test.eval(o);
        Assert.assertEquals("ok world", target);
    }

    @Test // "world 2 {hello}"
    public void simpleTemplateII() {
        String source = "world 2 {hello}";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.put("hello", "ok");
        String target = test.eval(o);
        Assert.assertEquals("world 2 ok", target);
    }

    @Test // "{hello}"
    public void simpleTemplateOneKey() {
        String source = "{hello}";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.put("hello", "world");
        String target = test.eval(o);
        Assert.assertEquals("world", target);
    }

    @Test // "{hello} {world}!"
    public void simpleTemplateMultipleKeys() {
        String source = "{hello} {world}!";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.put("hello", "hello");
        o.put("world", "world");
        String target = test.eval(o);
        Assert.assertEquals("hello world!", target);
    }

    @Test // "{hello {world}}!"
    public void simpleTemplateNested() {
        String source = "{hello {world}}!";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.put("world", "world");
        o.put("hello world", "hello world");
        String target = test.eval(o);
        Assert.assertEquals("hello world!", target);
    }

    @Test // "{hello {world {!}}}"
    public void simpleTemplateNestedDeep() {
        String source = "{hello {world {!}}}";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.put("!", "!");
        o.put("world !", "world !");
        o.put("hello world !", "ok");
        String target = test.eval(o);
        Assert.assertEquals("ok", target);
    }

    @Test // "{hello}{ok2}"
    public void onlyMultiKey() {
        String source = "{hello}{ok2}";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.put("hello", "hello");
        o.put("ok2", "test");
        String target = test.eval(o);
        Assert.assertEquals("hellotest", target);
    }

    @Test // "https://www.url.org?page={id}"
    public void tc1() {
        String source = "https://www.url.org?page={id}";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.put("id", 1);
        String target = test.eval(o);
        Assert.assertEquals("https://www.url.org?page=1", target);
    }

    @Test(expected = TemplateException.class) // "hell}o"
    public void bracesAsStringChars() {
        String source = "hell}o";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        test.eval(o);
    }

    @Test // "hell\\}o"
    public void bracesAsStringCharsEscaped() {
        String source = "hell\\}o";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        String target = test.eval(o);
        Assert.assertEquals("hell}o", target);
    }

    @Test
    public void multipleEscapes() {
        String source = "\\[he\\{\\}ll\\}o\\{\\}";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        String target = test.eval(o);
        Assert.assertEquals("[he{}ll}o{}", target);
    }

    @Test // "{L}[0]"
    public void simpleIndex() {
        String source = "{{L}}[0]";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.put("L", List.of("1","2","3"));
        String target = test.eval(o);
        Assert.assertEquals("1", target);
    }

    @Test // "{L}[{index}]"
    public void indexAsTemplate() {
        String source = "{{L}}[{index}]";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.put("L", List.of("1","2","3"));
        o.put("index", 0);
        String target = test.eval(o);
        Assert.assertEquals("1", target);
    }

    @Test(expected = TemplateException.class) // "{L}[{index}]"
    public void indexAsTemplateOOB() {
        String source = "{L}[{index}]";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.put("L", List.of("1","2","3"));
        o.put("index", 3);
        test.eval(o);
    }

    @Test // "{M}[ok]"
    public void simpleMapLookup() {
        String source = "{{M}}[ok]";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.put("M", Map.of("1", "hello world", "ok", "hello ok"));
        String target = test.eval(o);
        Assert.assertEquals("hello ok", target);
    }

    @Test
    public void dotInExpression() {
        String source = "{id}.json";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.put("id", "3");
        String target = test.eval(o);
        Assert.assertEquals("3.json", target);
    }

    @Test
    public void mixedExpression() {
        String source = "{A}{A}X{A}X";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.put("A", "X");
        String target = test.eval(o);
        Assert.assertEquals("XXXXX", target);
    }

    @Test
    public void dotInExpressionMixedTemplate() {
        String source = "{root}{date}/{id}.json";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.put("root", "/root");
        o.put("date", "1");
        o.put("id", "2");
        String target = test.eval(o);
        Assert.assertEquals("/root1/2.json", target);
    }

    @Test
    public void arrayLookupInString() {
        String source = "Mixed {{array}}[0]";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.put("array", List.of("2","3"));
        String target = test.eval(o);
        Assert.assertEquals("Mixed 2", target);
    }

    @Test
    public void nestedMapAndArrayLookup() {
        String source = "{{{array}}[0]}[module]";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.put("array", List.of(Map.of("module", "test-module")));
        String target = test.eval(o);
        Assert.assertEquals("test-module", target);
    }

//    @Test(timeout = 500)
//    public void aVeryLongString() {
//        String source = "hello world 123123123123 LONG                          string okhello world 123123123123 LO" +
//                "NG                          string okhello world 123123123123 LONG                          string " +
//                "okhello world 123123123123 LONG                          string okhello world 123123123123 LONG    " +
//                "                      string okhello world 123123123123 LONG                          string ok";
//        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
//        String goTo = test.eval(o);
//        Assert.assertEquals(source, goTo);
//    }

    @Test
    public void precedenceOfMixedTemplates() {
        String source = "{not-bound}{{bound}}[0]";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.put("bound", List.of("world"));
        o.put("not-bound", "hello ");
        String target = test.eval(o);
        Assert.assertEquals("hello world", target);
    }

//    @Test
//    public void complexGenericTemplate() {
//        String source = "{{L}}[{index}]";
//        TemplateExpression<List<String>> test = TemplateUtil.parseTemplate( source, new TypeToken<>() {} );
////        o.put("L", List.of(List.of(),List.of(),List.of(), List.of(1,2,3)));
////        o.put("index", 3);
//        Object tt = test.eval(o);
//    }
}