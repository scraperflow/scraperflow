package scraper.core.template;

import com.google.common.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.reflect.T;
import scraper.util.TemplateUtil;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TemplateExpTest {

    private final static FlowMap o = new FlowMapImpl();
    private List<Map<String, Integer>> t;

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
        o.output("hello", "ok");
        String target = test.eval(o);
        Assert.assertEquals("ok world", target);
    }

    @Test // "world 2 {hello}"
    public void simpleTemplateII() {
        String source = "world 2 {hello}";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.output("hello", "ok");
        String target = test.eval(o);
        Assert.assertEquals("world 2 ok", target);
    }

    @Test // "{hello}"
    public void simpleTemplateOneKey() {
        String source = "{hello}";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.output("hello", "world");
        String target = test.eval(o);
        Assert.assertEquals("world", target);
    }

    @Test // "{hello} {world}!"
    public void simpleTemplateMultipleKeys() {
        String source = "{hello} {world}!";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.output("hello", "hello");
        o.output("world", "world");
        String target = test.eval(o);
        Assert.assertEquals("hello world!", target);
    }

    @Test // "{hello {world}}!"
    public void simpleTemplateNested() {
        String source = "{hello {world}}!";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.output("world", "world");
        o.output("hello world", "hello world");
        String target = test.eval(o);
        Assert.assertEquals("hello world!", target);
    }

    @Test // "{hello {world {!}}}"
    public void simpleTemplateNestedDeep() {
        String source = "{hello {world {!}}}";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.output("!", "!");
        o.output("world !", "world !");
        o.output("hello world !", "ok");
        String target = test.eval(o);
        Assert.assertEquals("ok", target);
    }

    @Test // "{hello}{ok2}"
    public void onlyMultiKey() {
        String source = "{hello}{ok2}";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.output("hello", "hello");
        o.output("ok2", "test");
        String target = test.eval(o);
        Assert.assertEquals("hellotest", target);
    }

    @Test // "https://www.url.org?page={id}"
    public void tc1() {
        String source = "https://www.url.org?page={id}";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.output("id", "1");
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
        o.output("L", List.of("1","2","3"));
        String target = test.eval(o);
        Assert.assertEquals("1", target);
    }

    @Test // "{L}[{index}]"
    public void indexAsTemplate() {
        String source = "{{L}}[{index}]";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.output("L", List.of("1","2","3"));
        o.output("index", 0);
        String target = test.eval(o);
        Assert.assertEquals("1", target);
    }

    @Test(expected = TemplateException.class) // "{L}[{index}]"
    public void indexAsTemplateOOB() {
        String source = "{L}[{index}]";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.output("L", List.of("1","2","3"));
        o.output("index", 3);
        test.eval(o);
    }

    @Test // "{M}[ok]"
    public void simpleMapLookup() {
        String source = "{{M}}[ok]";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.output("M", Map.of("1", "hello world", "ok", "hello ok"));
        String target = test.eval(o);
        Assert.assertEquals("hello ok", target);
    }

    @Test
    public void dotInExpression() {
        String source = "{id}.json";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.output("id", "3");
        String target = test.eval(o);
        Assert.assertEquals("3.json", target);
    }

    @Test
    public void mixedExpression() {
        String source = "{A}{A}X{A}X";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.output("A", "X");
        String target = test.eval(o);
        Assert.assertEquals("XXXXX", target);
    }

    @Test
    public void dotInExpressionMixedTemplate() {
        String source = "{root}{date}/{id}.json";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.output("root", "/root");
        o.output("date", "1");
        o.output("id", "2");
        String target = test.eval(o);
        Assert.assertEquals("/root1/2.json", target);
    }

    @Test
    public void arrayLookupInString() {
        String source = "Mixed {{array}}[0]";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.output("array", List.of("2","3"));
        String target = test.eval(o);
        Assert.assertEquals("Mixed 2", target);
    }

    @Test
    public void nestedMapAndArrayLookup() {
        String source = "{{{array}}[0]}[module]";
        TemplateExpression<String> test = TemplateUtil.parseTemplate(source, TypeToken.of(String.class));
        o.output("array", List.of(Map.of("module", "test-module")));
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
        o.output("bound", List.of("world"));
        o.output("not-bound", "hello ");
        String target = test.eval(o);
        Assert.assertEquals("hello world", target);
    }

    @Test(expected = TemplateException.class)
    public void complexGenericTemplateFail() {
        String source = "{{L}}[{index}]";
        TemplateExpression<List<Integer>> test = TemplateUtil.parseTemplate( source, new TypeToken<>() {} );
        o.output(new T<>("L"){}, List.of(List.of("1"), List.of("2"), List.of("3")));
        o.output(new T<>("index"){}, 2);
        test.eval(o);
    }

    @Test
    public void complexGenericTemplate() {
        String source = "{{L}}[{index}]";
        TemplateExpression<List<String>> test = TemplateUtil.parseTemplate( source, new TypeToken<>() {} );
        o.output(new T<>("L"){}, List.of(List.of("1"), List.of("2"), List.of("3")));
        o.output(new T<>("index"){}, 2);
        //noinspection unused
        List<String> tt = test.eval(o);
    }

    @Test
    public void complexGenericTemplateOnlySubtype() {
        String source = "{{L}}[{index}]";
        TemplateExpression<List<?>> test = TemplateUtil.parseTemplate( source, new TypeToken<>(){});
        o.output(new T<>("L") {}, List.of(List.of("1"), List.of("2"), List.of("3")));
        o.output(new T<>("index") {}, 2);

        try {
            List<?> tt = test.eval(o);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void genericSubtyping() {
        TypeToken<List<String>> stringList  = new TypeToken<>(){};
        TypeToken<List<Object>> moreGenericList  = new TypeToken<>(){};
        TypeToken<List<? extends Object>> mostGenericType  = new TypeToken<>(){};

        // generic subtyping requires wildcards
        Assert.assertFalse(stringList.isSubtypeOf(moreGenericList));

        // wildcard extension should hold true
        Assert.assertTrue(moreGenericList.isSubtypeOf(mostGenericType));

        // string extends object wildcard OK
        Assert.assertTrue(stringList.isSubtypeOf(mostGenericType));
    }

    @Test
    public void genericSubtypingNested() {
        // enter nested madness
        TypeToken<List<List<String>>> stringList  = new TypeToken<>(){};
        TypeToken<List<List<Object>>> moreGenericList  = new TypeToken<>(){};
        TypeToken<List<? extends List<? extends Object>>> mostGenericType  = new TypeToken<>(){};

        // generic subtyping requires wildcards
        Assert.assertFalse(stringList.isSubtypeOf(moreGenericList));

        // wildcard extension should hold true
        Assert.assertTrue(moreGenericList.isSubtypeOf(mostGenericType));

        // string extends object wildcard OK
        Assert.assertTrue(stringList.isSubtypeOf(mostGenericType));

        //noinspection TypeParameterExplicitlyExtendsObject without this the check is not working
        TypeToken<List<? extends Object>> lessGenericList  = new TypeToken<>(){};
        // OK
        Assert.assertTrue(lessGenericList.isSupertypeOf(mostGenericType));
    }

    @Test
    public void genericSubtypingWildcard() {
        TypeToken<String> string  = new TypeToken<String>(){};
        TypeToken<?> object  = new TypeToken<Object>(){};
        Assert.assertTrue(string.isSubtypeOf(object));
        Assert.assertFalse(object.isSubtypeOf(string));
        Assert.assertTrue(object.isSupertypeOf(string));


        TypeToken<List<String>> liststr  = new TypeToken<List<String>>(){};
        TypeToken<List<?>> targetList = new TypeToken<>() {};
        Assert.assertTrue(targetList.isSupertypeOf(liststr));

        TypeToken<List<List<String>>> listliststr  = new TypeToken<List<List<String>>>(){};
        TypeToken<List<? extends List<?>>> genericlislist = new TypeToken<>(){};

        Assert.assertTrue(genericlislist.isSupertypeOf(listliststr));
        Assert.assertTrue(targetList.isSupertypeOf(listliststr));
    }

    @Test
    public void wildcardTest() {
        TypeToken<List<List<String>>> known  = new TypeToken<>(){};
        TypeToken<List<?>> expected = new TypeToken<>() {};

        Assert.assertTrue(expected.isSupertypeOf(known));

        TemplateExpression<List<?>> expectedTemplate = TemplateUtil.parseTemplate( "{L}", new TypeToken<>(){});
        TypeToken<List<?>> expectedToken = expectedTemplate.targetType;
        Assert.assertTrue(expectedToken.isSupertypeOf(known));

        TypeToken<List<? extends List<?>>> expectedNested = new TypeToken<>() {};
        Assert.assertTrue(expectedNested.isSupertypeOf(known));

        o.output(new T<>("L") {}, List.of(List.of("1"), List.of("2"), List.of("3")));
        TypeToken<List<? extends List<?>>> expected2 = new TypeToken<>() {};
        ((FlowMapImpl) o).getWithType("L", expected2);

        TypeToken<List<?>> expected3 = new TypeToken<>() {};
        ((FlowMapImpl) o).getWithType("L", expected3);

        expectedTemplate.eval(o);
    }

    @Test
    public void wildcardTest2() {
        TemplateExpression<List<? extends List<?>>> expectedTemplate = TemplateUtil.parseTemplate( "{L}", new TypeToken<>(){});
        o.output(new T<>("L") {}, List.of(List.of("1"), List.of("2"), List.of("3")));
        expectedTemplate.eval(o);
    }

    @Test
    public void wildcardTest3() {
        TemplateExpression<List<?>> expectedTemplate = TemplateUtil.parseTemplate( "{{L}}[2]", new TypeToken<>(){});
        o.output(new T<>("L") {}, List.of(List.of("1"), List.of("2"), List.of("3")));
        expectedTemplate.eval(o);
    }

    @Test
    public void outputGenericInputSpecificTest() {
        TemplateExpression<List<?>> expectedTemplate = TemplateUtil.parseTemplate( "{{L}}[2]", new TypeToken<>(){});
        o.output("L", List.of(List.of("1"), List.of("2"), List.of("3")));
        List<?> l = expectedTemplate.eval(o);
        Assert.assertEquals("3", l.get(0));
    }

    @Test(expected = TemplateException.class)
    public void outputGenericInputSpecificBadTest() {
        TemplateExpression<List<Map<String, Integer>>> expectedTemplate = TemplateUtil.parseTemplate( "{{L}}[2]", new TypeToken<>(){});
        o.output("L", List.of(List.of("1"), List.of("2"), List.of("3")));
        List<Map<String, Integer>> tt = expectedTemplate.eval(o);
    }
}