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
import scraper.util.TemplateUtil;

import java.util.List;
import java.util.Map;

public class TemplateExpTest {

    private final static FlowMap o = new FlowMapImpl();

    @Before
    public void clean() { o.clear(); }

    // ===============
    // SIMPLE TEMPLATE
    // ===============

    // == Eval

    @Test // "hello world"
    public void simpleTemplateNoKeys() {
        String source = "hello world";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        String target = test.eval(o);
        Assert.assertEquals("hello world", target);
    }

    @Test // "{hello} world"
    public void simpleTemplateKeyAndString() {
        String source = "{hello} world";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("hello", "ok");
        String target = test.eval(o);
        Assert.assertEquals("ok world", target);
    }

    @Test // "world 2 {hello}"
    public void simpleTemplateII() {
        String source = "world 2 {hello}";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("hello", "ok");
        String target = test.eval(o);
        Assert.assertEquals("world 2 ok", target);
    }

    @Test // "{hello}"
    public void simpleTemplateOneKey() {
        String source = "{hello}";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("hello", "world");
        String target = test.eval(o);
        Assert.assertEquals("world", target);
    }

    @Test // "{hello} {world}!"
    public void simpleTemplateMultipleKeys() {
        String source = "{hello} {world}!";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("hello", "hello");
        o.output("world", "world");
        String target = test.eval(o);
        Assert.assertEquals("hello world!", target);
    }

    @Test // "{hello {world}}!"
    public void simpleTemplateNested() {
        String source = "{hello {world}}!";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("world", "world");
        o.output("hello world", "hello world");
        String target = test.eval(o);
        Assert.assertEquals("hello world!", target);
    }

    @Test // "{hello {world {!}}}"
    public void simpleTemplateNestedDeep() {
        String source = "{hello {world {!}}}";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("!", "!");
        o.output("world !", "world !");
        o.output("hello world !", "ok");
        String target = test.eval(o);
        Assert.assertEquals("ok", target);
    }

    @Test // "{hello}{ok2}"
    public void onlyMultiKey() {
        String source = "{hello}{ok2}";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("hello", "hello");
        o.output("ok2", "test");
        String target = test.eval(o);
        Assert.assertEquals("hellotest", target);
    }

    @Test // "https://www.url.org?page={id}"
    public void tc1() {
        String source = "https://www.url.org?page={id}";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("id", "1");
        String target = test.eval(o);
        Assert.assertEquals("https://www.url.org?page=1", target);
    }

    @Test(expected = TemplateException.class) // "hell}o"
    public void bracesAsStringChars() {
        String source = "hell}o";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        test.eval(o);
    }

    @Test // "hell\\}o"
    public void bracesAsStringCharsEscaped() {
        String source = "hell\\}o";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        String target = test.eval(o);
        Assert.assertEquals("hell}o", target);
    }

    @Test
    public void multipleEscapes() {
        String source = "\\[he\\{\\}ll\\}o\\{\\}";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        String target = test.eval(o);
        Assert.assertEquals("[he{}ll}o{}", target);
    }

    @Test
    public void simpleIndex() {
        String source = "{{L}}[0]";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("L", List.of("1","2","3"));
        String target = test.eval(o);
        Assert.assertEquals("1", target);
    }

    @Test
    public void indexAsTemplate() {
        String source = "{{L}}[{index}]";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("L", List.of("1","2","3"));
        o.output("index", 0);
        String target = test.eval(o);
        Assert.assertEquals("1", target);
    }

    @Test(expected = TemplateException.class)
    public void indexAsTemplateOOB() {
        String source = "{L}[{index}]";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("L", List.of("1","2","3"));
        o.output("index", 3);
        test.eval(o);
    }

    @Test
    public void simpleMapLookup() {
        String source = "{{M}@ok}";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("M", Map.of("1", "hello world", "ok", "hello ok"));
        String target = test.eval(o);
        Assert.assertEquals("hello ok", target);
    }

    @Test
    public void dotInExpression() {
        String source = "{id}.json";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("id", "3");
        String target = test.eval(o);
        Assert.assertEquals("3.json", target);
    }

    @Test
    public void mixedExpression() {
        String source = "{A}{A}X{A}X";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("A", "X");
        String target = test.eval(o);
        Assert.assertEquals("XXXXX", target);
    }

    @Test
    public void dotInExpressionMixedTemplate() {
        String source = "{root}{date}/{id}.json";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("root", "/root");
        o.output("date", "1");
        o.output("id", "2");
        String target = test.eval(o);
        Assert.assertEquals("/root1/2.json", target);
    }

    @Test
    public void arrayLookupInString() {
        String source = "Mixed {{array}}[0]";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("array", List.of("2","3"));
        String target = test.eval(o);
        Assert.assertEquals("Mixed 2", target);
    }

    @Test
    public void nestedMapAndArrayLookup() {
        String source = "{{{array}}[0]@module}";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("array", List.of(Map.of("module", "test-module")));
        String target = test.eval(o);
        Assert.assertEquals("test-module", target);
    }

    @Test
    public void nestedMapLookup() {
        String source = "{{map}@first}";
        Term<Map<String, String>> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("map", Map.of("first", Map.of("module", "test-module")));
        Map<String, String> target = test.eval(o);
        Assert.assertFalse(target.isEmpty());

        String source2 = "{{{map}@first}@module}";
        Term<String> test2 = TemplateUtil.parseTemplate(source2, new T<>(){});
        String target2 = test2.eval(o);
        Assert.assertEquals("test-module", target2);
    }

    @Test
    public void concatMapLookup() {
        String source = "{{map}@first}{{map}@second}";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("map", Map.of("first", "hello", "second", "world"));
        String target = test.eval(o);
        Assert.assertEquals("helloworld", target);
    }

//    @Test(timeout = 500)
//    public void aVeryLongString() {
//        String source = "hello world 123123123123 LONG                          string okhello world 123123123123 LO" +
//                "NG                          string okhello world 123123123123 LONG                          string " +
//                "okhello world 123123123123 LONG                          string okhello world 123123123123 LONG    " +
//                "                      string okhello world 123123123123 LONG                          string ok";
//        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
//        String goTo = test.eval(o);
//        Assert.assertEquals(source, goTo);
//    }

    @Test
    public void precedenceOfMixedTemplates() {
        String source = "{not-bound}{{bound}}[0]";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("bound", List.of("world"));
        o.output("not-bound", "hello ");
        String target = test.eval(o);
        Assert.assertEquals("hello world", target);
    }

    @Test(expected = TemplateException.class)
    public void complexGenericTemplateFail() {
        String source = "{{L}}[{index}]";
        Term<List<Integer>> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("L", List.of(List.of("1"), List.of("2"), List.of("3")));
        o.output("index", 2);
        test.eval(o);
    }

    @Test
    public void complexGenericTemplate() {
        String source = "{{L}}[{index}]";
        Term<List<String>> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("L", List.of(List.of("1"), List.of("2"), List.of("3")));
        o.output("index", 2);
        //noinspection unused
        List<String> tt = test.eval(o);
    }

    @Test
    public void complexGenericTemplateOnlySubtype() {
        String source = "{{L}}[{index}]";
        Term<List<?>> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("L", List.of(List.of("1"), List.of("2"), List.of("3")));
        o.output("index", 2);

        test.eval(o);
    }

    @Test
    public void genericSubtyping() {
        TypeToken<List<String>> stringList  = new TypeToken<>(){};
        TypeToken<List<Object>> moreGenericList  = new TypeToken<>(){};
        @SuppressWarnings("TypeParameterExplicitlyExtendsObject") // testing
        TypeToken<List<? extends Object>> mostGenericType  = new TypeToken<>(){};

        // generic subtyping requires wildcards
        Assert.assertFalse(stringList.isSubtypeOf(moreGenericList));

        // wildcard extension should hold true
        Assert.assertTrue(moreGenericList.isSubtypeOf(mostGenericType));

        // string extends object wildcard OK
        Assert.assertTrue(stringList.isSubtypeOf(mostGenericType));
    }

    @SuppressWarnings("TypeParameterExplicitlyExtendsObject") // testing
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

    @SuppressWarnings("Convert2Diamond") // testing
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
//        TypeToken<List<List<String>>> known  = new TypeToken<>(){};
//        TypeToken<List<?>> expected = new TypeToken<>() {};
//
//        Assert.assertTrue(expected.isSupertypeOf(known));
//
//        Term<List<?>> expectedTemplate = TemplateUtil.parseTemplate( "{L}", new T<>(){});
//        TypeToken<List<?>> expectedToken = expectedTemplate.targetType;
//        Assert.assertTrue(expectedToken.isSupertypeOf(known));
//
//        TypeToken<List<? extends List<?>>> expectedNested = new TypeToken<>() {};
//        Assert.assertTrue(expectedNested.isSupertypeOf(known));
//
//        o.output(new T<>("L") {}, List.of(List.of("1"), List.of("2"), List.of("3")));
//        T<List<? extends List<?>>> expected2 = new T<>() {};
//        o.getWithType("L", expected2);
//
//        T<List<?>> expected3 = new T<>() {};
//        o.getWithType("L", expected3);
//
//        expectedTemplate.eval(o);
    }

    @Test
    public void wildcardTest2() {
        Term<List<? extends List<?>>> expectedTemplate = TemplateUtil.parseTemplate( "{L}", new T<>(){});
        o.output("L", List.of(List.of("1"), List.of("2"), List.of("3")));
        expectedTemplate.eval(o);
    }

    @Test
    public void wildcardTest3() {
        Term<List<?>> expectedTemplate = TemplateUtil.parseTemplate( "{{L}}[2]", new T<>(){});
        o.output("L", List.of(List.of("1"), List.of("2"), List.of("3")));
        expectedTemplate.eval(o);
    }

    @Test
    public void outputGenericInputSpecificSingleLayerTest() {
        Term<?> expectedTemplate = TemplateUtil.parseTemplate( "{L}", new T<>(){});
        o.output("L", List.of("1"));
        Object l = expectedTemplate.eval(o);
        Assert.assertNotNull(l);
    }

    @Test
    public void logTest() {
        Term<Object> log = TemplateUtil.parseTemplate( "{r}/test.o", new T<>(){});
        o.output("r", "hello");
        Object l = log.eval(o);
        Assert.assertEquals("hello/test.o", l);
    }

    @Test
    public void outputGenericInputSpecificTest() {
        Term<List<?>> expectedTemplate = TemplateUtil.parseTemplate( "{{L}}[2]", new T<>(){});
        o.output("L", List.of(List.of("1"), List.of("2"), List.of("3")));
        List<?> l = expectedTemplate.eval(o);
        Assert.assertEquals("3", l.get(0));
    }

    @Test(expected = TemplateException.class)
    public void outputGenericInputSpecificBadTest() {
        Term<List<Map<String, Integer>>> expectedTemplate = TemplateUtil.parseTemplate( "{{L}}[2]", new T<>(){});
        o.output("L", List.of(List.of("1"), List.of("2"), List.of("3")));
        List<Map<String, Integer>> tt = expectedTemplate.eval(o);
        System.out.println(tt);
    }
}