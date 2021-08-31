package scraper.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scraper.api.TemplateException;
import scraper.api.ValidationException;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.T;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static scraper.util.TemplateUtil.parseTemplate;

public class TemplateTest {

    private final static FlowMapImpl o = new FlowMapImpl();
    private final static ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void clean() { o.clear(); }

    // ===============
    // SIMPLE TEMPLATE
    // ===============

    // == Eval

    @Test // "hello"
    public void simpleTemplateNoKeys() {
        T<String> simpleString = new T<>(){};
        simpleString.setTerm(parseTemplate("hello", simpleString));

        String eval = o.eval(simpleString);
        assertEquals("hello", eval);
        assertNotNull(simpleString.toString());
    }

    @Test // ""
    public void simpleTEmpty() {
        T<String> str = new T<>(){};
        str.setTerm(parseTemplate("", str));
        assertEquals("", o.eval(str));
    }

    @Test // "{hello}" // hello
    public void simpleTSingle() {
        T<String> str = new T<>(){};
        str.setTerm(parseTemplate("{hello}", str));
        o.output("hello", "eval-value");
        String eval = o.eval(str);
        assertEquals("eval-value", eval);
    }

    @Test // "{int}" // hello
    public void simpleTSingleInt() {
        T<Integer> str = new T<>(){};
        str.setTerm(parseTemplate("{int}", str));
        o.output("int", 1);
        Integer eval = o.eval(str);
        assertEquals((Integer) 1, eval);
    }

    @Test // "{hello}{hello}hello // hello
    public void simpleTDouble() {
        T<String> str = new T<>(){};
        str.setTerm(parseTemplate("{hello}{hello}hello", str));
        o.output("hello", "eval-value");
        String eval = o.eval(str);
        assertEquals("eval-valueeval-valuehello", eval);
        assertNotNull(str.toString());
    }

    @Test
    public void wrongTarget() {
        assertThrows(TemplateException.class, () -> {
            T<List<String>> str = new T<>(){};
            str.setTerm(parseTemplate("{hello}{hello}hello", str));
        });
    }

    @Test // "{hello}hello{hello}hello // hello
    public void simpleTMixed() {
        T<String> str = new T<>(){};
        str.setTerm(parseTemplate("{hello}hello{hello}hello", str));
        o.output("hello", "eval-value");
        String eval = o.eval(str);
        assertEquals("eval-valuehelloeval-valuehello", eval);
        assertNotNull(str.toString());
    }

    @SuppressWarnings("unchecked") // mapper
    @Test
    public void simpleTNestedInList() throws Exception {
        T<List<Integer>> str = new T<>(){};
        str.setTerm(parseTemplate(mapper.readValue("[1,2,\"{template-int}\"]", List.class), str));
        o.output("template-int",3);
        List<Integer> eval = o.eval(str);
        assertEquals((Integer) 1, eval.get(0));
        assertEquals((Integer) 2, eval.get(1));
        assertEquals((Integer) 3, eval.get(2));
    }

    @SuppressWarnings("unchecked") // object mapper
    @Test // "{\"1\": 2, \"other\": \"{template-int}\"}" // template-int
    public void simpleTNestedInMap() throws Exception {
        T<Map<String, Integer>> str = new T<>(){};
        str.setTerm(parseTemplate( mapper.readValue("{\"1\": 2, \"other\": \"{template-int}\"}", Map.class), str));
        o.output("template-int",3);
        Map<String, Integer> eval = o.eval(str);
        assertEquals((Integer) 2, eval.get("1"));
        assertEquals((Integer) 3, eval.get("other"));
    }

    // == Bad

    @Test
    public void simpleTMissingKey() {
        assertThrows(TemplateException.class, () -> {
            T<String> str = new T<>(){};
            str.setTerm(parseTemplate("{hello}", str));
            o.eval(str);
        });
    }

    @Test
    public void simpleTBadKeyType() {
        assertThrows(ClassCastException.class, () -> {
            T<Map<String, Integer>> str = new T<>() {};
            str.setTerm(parseTemplate(mapper.readValue("{\"other\": \"{template-int}\"}", Map.class), str));
            o.output("template-int", "3b");
            Map<String, Integer> eval = o.eval(str);
            assertEquals((Integer) 3, eval.get("other"));
        });
    }

//    // ===============
//    // FM Lookup
//    // ===============

    @Test
    public void badConversionTest() {
        assertThrows(ValidationException.class, () -> {
            class TNotImplemented implements Serializable {}
            T<String> str = new T<>(){};
            str.setTerm(parseTemplate(new TNotImplemented(), str));
        });
    }

    @Test
    public void nestedKeyLookupFail() {
        assertThrows(ClassCastException.class, () -> { // not a TemplateException,
            // only ClassCastException since TypeChecking happens at compile time
            T<String> str = new T<>(){};
            str.setTerm(parseTemplate(mapper.readValue("\"{list}\"}", String.class), str));
            o.output("list", List.of("hello"));
            String eval = o.eval(str);
        });
    }

    @Test
    public void nestedArrayLookup() throws Exception {
        T<String> str = new T<>(){};
        str.setTerm(parseTemplate(mapper.readValue("\"{[list^0]}\"}", String.class), str));
        o.output("list", List.of("hello"));
        o.output("hello", "1");
        String eval = o.eval(str);
        assertEquals("1", eval);
    }

    @Test
    public void outOfBoundsArrayLookup() throws Exception {
        assertThrows(TemplateException.class, () -> {
            T<String> str = new T<>(){};
            str.setTerm(parseTemplate(mapper.readValue("\"{{list}}[1]\"}", String.class), str));
            o.output("list", List.of("hello"));
            o.eval(str);
        });
    }

    @Test
    public void arrayNegativeLookup() throws Exception {
        T<String> str = new T<>(){};
        str.setTerm(parseTemplate(mapper.readValue("\"[list^-1]\"}", String.class), str));
        o.output("list", List.of("hello", "world"));
        String eval = o.eval(str);
        assertEquals("world", eval);
    }

    @SuppressWarnings("unchecked") // mapper
    @Test
    public void nestedMapEvalTest() throws Exception {
        T<Map<String, Integer>> str = new T<>(){};
        str.setTerm(parseTemplate(mapper.readValue("{\"other\": \"{{template-int}}\"}", Map.class), str));
        o.output("template-int","3b");
        o.output("3b",3);
        Map<String, Integer> eval = o.eval(str);
        assertEquals((Integer) 3, eval.get("other"));
    }

    @Test
    public void simpleOutputTemplate() {
        T<String> simpleString = new T<>(){};
        simpleString.setTerm(parseTemplate("hello", simpleString));

        String eval = o.eval(simpleString);
        assertEquals("hello", eval);
        assertNotNull(simpleString.toString());
    }

    @Test
    public void regexTest() {
        String regex = "\\{(\\w+),\\s*start_link,";

        T<String> simpleString = new T<>(){};
        simpleString.setTerm(parseTemplate(regex, simpleString));

        String eval = o.eval(simpleString);
        assertEquals(regex.substring(1), eval);
    }

    @Test
    public void escapeChars() {
        String template = "\\^\\{\\}\\@\\[\\]";

        T<String> s = new T<>(){};
        s.setTerm(parseTemplate(template, s));

        String eval = o.eval(s);
        assertEquals("^{}@[]", eval);
    }

    @Test
    public void linebreak() {
        String template = "\n";

        T<String> s = new T<>(){};
        s.setTerm(parseTemplate(template, s));

        String eval = o.eval(s);
        assertEquals("\n", eval);
    }

    @Test
    public void nullstring() {
        String template = "\0";

        T<String> s = new T<>(){};
        s.setTerm(parseTemplate(template, s));

        String eval = o.eval(s);
        assertEquals("\0", eval);
    }
}