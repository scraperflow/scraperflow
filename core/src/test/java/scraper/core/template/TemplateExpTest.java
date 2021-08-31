package scraper.core.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scraper.api.TemplateException;
import scraper.api.ValidationException;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.T;
import scraper.api.Term;
import scraper.util.TemplateUtil;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateExpTest {

    private final static FlowMapImpl o = new FlowMapImpl();

    @BeforeEach
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
        assertEquals("hello world", target);
    }

    @Test // "{hello} world"
    public void simpleTemplateKeyAndString() {
        String source = "{hello} world";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("hello", "ok");
        String target = test.eval(o);
        assertEquals("ok world", target);
    }

    @Test // "world 2 {hello}"
    public void simpleTemplateII() {
        String source = "world 2 {hello}";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("hello", "ok");
        String target = test.eval(o);
        assertEquals("world 2 ok", target);
    }

    @Test // "{hello}"
    public void simpleTemplateOneKey() {
        String source = "{hello}";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("hello", "world");
        String target = test.eval(o);
        assertEquals("world", target);
    }

    @Test // "{hello} {world}!"
    public void simpleTemplateMultipleKeys() {
        String source = "{hello} {world}!";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("hello", "hello");
        o.output("world", "world");
        String target = test.eval(o);
        assertEquals("hello world!", target);
    }

    @Test // "{hello {world}}!"
    public void simpleTemplateNested() {
        String source = "{hello {world}}!";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("world", "world");
        o.output("hello world", "hello world");
        String target = test.eval(o);
        assertEquals("hello world!", target);
    }

    @Test // "{hello {world {!}}}"
    public void simpleTemplateNestedDeep() {
        String source = "{hello {world {!}}}";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("!", "!");
        o.output("world !", "world !");
        o.output("hello world !", "ok");
        String target = test.eval(o);
        assertEquals("ok", target);
    }

    @Test // "{hello}{ok2}"
    public void onlyMultiKey() {
        String source = "{hello}{ok2}";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("hello", "hello");
        o.output("ok2", "test");
        String target = test.eval(o);
        assertEquals("hellotest", target);
    }

    @Test // "https://www.url.org?page={id}"
    public void tc1() {
        String source = "https://www.url.org?page={id}";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("id", "1");
        String target = test.eval(o);
        assertEquals("https://www.url.org?page=1", target);
    }

    @Test // "hell}o"
    public void bracesAsStringChars() {
        assertThrows(TemplateException.class, () -> {
            String source = "hell}o";
            Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
            test.eval(o);
                });
    }

    @Test // "hell\\}o"
    public void bracesAsStringCharsEscaped() {
        String source = "hell\\}o";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        String target = test.eval(o);
        assertEquals("hell}o", target);
    }

    @Test
    public void multipleEscapes() {
        String source = "\\[he\\{\\}ll\\}o\\{\\}\\";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        String target = test.eval(o);
        assertEquals("[he{}ll}o{}\\", target);
    }

    @Test
    public void simpleIndex() {
        String source = "[L^0]";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("L", List.of("1","2","3"));
        String target = test.eval(o);
        assertEquals("1", target);
    }

    @Test
    public void indexAsTemplate() {
        String source = "[L^{index}]";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("L", List.of("1","2","3"));
        o.output("index", 0);
        String target = test.eval(o);
        assertEquals("1", target);
    }

    @Test
    public void indexAsTemplateOOB() {
        assertThrows(TemplateException.class, () -> {
            String source = "[L^{index}]";
            Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
            o.output("L", List.of("1","2","3"));
            o.output("index", 3);
            test.eval(o);
                });
    }

    @Test
    public void simpleMapLookup() {
        String source = "{M@ok}";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("M", Map.of("1", "hello world", "ok", "hello ok"));
        String target = test.eval(o);
        assertEquals("hello ok", target);

        System.out.println(test.getRaw());
    }

    @Test
    public void simpleMapLookupFail() {
        assertThrows(TemplateException.class, () -> {
            String source = "{M@ok}";
            Term<String> test = TemplateUtil.parseTemplate(source, new T<>() {
            });
            o.output("M", Map.of("1", "hello world", "nk", "hello ok"));
            test.eval(o);
        });
    }

    @Test
    public void dotInExpression() {
        String source = "{id}.json";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("id", "3");
        String target = test.eval(o);
        assertEquals("3.json", target);
    }

    @Test
    public void mixedExpression() {
        String source = "{A}{A}X{A}X";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("A", "X");
        String target = test.eval(o);
        assertEquals("XXXXX", target);
    }

    @Test
    public void dotInExpressionMixedTemplate() {
        String source = "{root}{date}/{id}.json";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("root", "/root");
        o.output("date", "1");
        o.output("id", "2");
        String target = test.eval(o);
        assertEquals("/root1/2.json", target);
    }

    @Test
    public void arrayLookupInString() {
        String source = "Mixed [array^0]";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("array", List.of("2","3"));
        String target = test.eval(o);
        assertEquals("Mixed 2", target);
    }

    @Test
    public void nestedMapAndArrayLookup() {
        String source = "{[array^0]@module}";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("array", List.of(Map.of("module", "test-module")));
        String target = test.eval(o);
        assertEquals("test-module", target);
    }

    @Test
    public void nestedMapLookup() {
        String source = "{map@first}";
        Term<Map<String, String>> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("map", Map.of("first", Map.of("module", "test-module")));
        Map<String, String> target = test.eval(o);
        assertFalse(target.isEmpty());

        String source2 = "{{map@first}@module}";
        Term<String> test2 = TemplateUtil.parseTemplate(source2, new T<>(){});
        String target2 = test2.eval(o);
        assertEquals("test-module", target2);
    }

    @Test
    public void concatMapLookup() {
        String source = "{map@first}{map@second}";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("map", Map.of("first", "hello", "second", "world"));
        String target = test.eval(o);
        assertEquals("helloworld", target);
    }

    @Test
    public void aVeryLongString() {
        String source = "hello world 123123123123 LONG                          string okhello world 123123123123 LO" +
                "NG                          string okhello world 123123123123 LONG                          string " +
                "okhello world 123123123123 LONG                          string okhello world 123123123123 LONG    " +
                "                      string okhello world 123123123123 LONG                          string ok";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        String goTo = test.eval(o);
        assertEquals(source, goTo);
    }

    @Test
    public void precedenceOfMixedTemplates() {
        String source = "{not-bound}[bound^0]";
        Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("bound", List.of("world"));
        o.output("not-bound", "hello ");
        String target = test.eval(o);
        assertEquals("hello world", target);
    }

    @Test // this should evaluate but is prohibited by static type checking
    public void complexGenericTemplateFail() {
        String source = "[L^{index}]";
        Term<List<Integer>> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("L", List.of(List.of("1"), List.of("2"), List.of("3")));
        o.output("index", 2);
        test.eval(o);
    }

    @Test
    public void complexGenericTemplate() {
        String source = "[L^{index}]";
        Term<List<String>> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("L", List.of(List.of("1"), List.of("2"), List.of("3")));
        o.output("index", 2);
        //noinspection unused
        List<String> tt = test.eval(o);
    }

    @Test
    public void complexGenericTemplateOnlySubtype() {
        String source = "[L^{index}]";
        Term<List<?>> test = TemplateUtil.parseTemplate(source, new T<>(){});
        o.output("L", List.of(List.of("1"), List.of("2"), List.of("3")));
        o.output("index", 2);

        test.eval(o);
    }


    @Test
    public void wildcardTest2() {
        Term<List<? extends List<?>>> expectedTemplate = TemplateUtil.parseTemplate( "{L}", new T<>(){});
        o.output("L", List.of(List.of("1"), List.of("2"), List.of("3")));
        expectedTemplate.eval(o);
    }

    @Test
    public void wildcardTest3() {
        Term<List<?>> expectedTemplate = TemplateUtil.parseTemplate( "[L^2]", new T<>(){});
        o.output("L", List.of(List.of("1"), List.of("2"), List.of("3")));
        expectedTemplate.eval(o);
    }

    @Test
    public void outputGenericInputSpecificSingleLayerTest() {
        Term<?> expectedTemplate = TemplateUtil.parseTemplate( "{L}", new T<>(){});
        o.output("L", List.of("1"));
        Object l = expectedTemplate.eval(o);
        assertNotNull(l);
    }

    @Test
    public void logTest() {
        Term<Object> log = TemplateUtil.parseTemplate( "{r}/test.o", new T<>(){});
        o.output("r", "hello");
        Object l = log.eval(o);
        assertEquals("hello/test.o", l);
    }

    @Test
    public void outputGenericInputSpecificTest() {
        Term<List<?>> expectedTemplate = TemplateUtil.parseTemplate( "[L^2]", new T<>(){});
        o.output("L", List.of(List.of("1"), List.of("2"), List.of("3")));
        List<?> l = expectedTemplate.eval(o);
        assertEquals("3", l.get(0));
        assertEquals("[{L}^2]", expectedTemplate.getRaw());
        assertEquals("java.util.List<?>",expectedTemplate.getToken().get().getTypeName());
    }

    @Test // this should evaluate but is prohibited by static type checking
    public void outputGenericInputSpecificBadTest() {
        Term<List<Map<String, Integer>>> expectedTemplate = TemplateUtil.parseTemplate( "[L^2]", new T<>(){});
        o.output("L", List.of(List.of("1"), List.of("2"), List.of("3")));
        List<Map<String, Integer>> tt = expectedTemplate.eval(o);
        System.out.println(tt);
    }

    @Test
    public void listTemplateTest() throws ValidationException {
        Term<List<String>> listTemplate = TemplateUtil.parseTemplate( List.of("1", "2"), new T<>(){});
        List<String> tt = listTemplate.eval(o);
        assertEquals(tt, listTemplate.getRaw());
        assertEquals("java.util.List<java.lang.String>", listTemplate.getToken().get().getTypeName());
    }

    @Test
    public void mapTemplateTest() throws ValidationException {
        Term<Map<String, String>> mapTemplate = TemplateUtil.parseTemplate( Map.of("1", "2"), new T<>(){});
        Map<String, String> tt = mapTemplate.eval(o);
        assertEquals(tt, mapTemplate.getRaw());
        assertEquals("java.util.Map<java.lang.String, java.lang.String>", mapTemplate.getToken().get().getTypeName());
    }

    @Test
    public void constantTest() throws ValidationException {
        Term<Object> test = TemplateUtil.parseTemplate(new Object(), new T<>(){});
        System.out.println(test.getRaw());
        test.eval(o);

        assertEquals(test.getToken(), new T<>(){});
    }

    @Test
    public void badMixedTemplate() {
        assertThrows(TemplateException.class, () -> {
            String source = "world {hello}";
            Term<String> test = TemplateUtil.parseTemplate(source, new T<>(){});
            test.eval(o);
        });
    }
}