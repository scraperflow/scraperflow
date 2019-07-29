package scraper.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scraper.api.exceptions.TemplateException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.util.NodeUtil;

import java.io.IOException;
import java.util.*;

import static scraper.util.NodeUtil.convert;

public class TemplateTest {

    private final static FlowMap o = NodeUtil.flowOf(new HashMap<>());
    private final static ObjectMapper mapper = new ObjectMapper();

    @Before
    public void clean() { o.clear(); }

    // ===============
    // SIMPLE TEMPLATE
    // ===============

    // == Eval

    @Test // "hello"
    public void simpleTemplateNoKeys() {
        Template<String> simpleString = new Template<>(){};
        simpleString.setParsedJson("hello");
        String eval = simpleString.eval(o);
        Assert.assertEquals("hello", eval);
        Assert.assertNotNull(simpleString.toString());
    }

    @Test // ""
    public void simpleTemplateEmpty() throws ValidationException {
        Template<String> str = new Template<>(){};
        str.setParsedJson(convert(str.type, ""));
        Assert.assertEquals("", str.eval(o));
    }

    @Test // "{hello}" // hello
    public void simpleTemplateSingle() throws Exception {
        Template<String> str = new Template<>(){};
        str.setParsedJson(convert(str.type, "{hello}"));
        o.put("hello", "eval-value");
        String eval = str.eval(o);
        Assert.assertEquals("eval-value", eval);
    }

    @Test // "{int}" // hello
    public void simpleTemplateSingleInt() throws Exception {
        Template<Integer> str = new Template<>(){};
        str.setParsedJson(convert(str.type, "{int}"));
        o.put("int", 1);
        Integer eval = str.eval(o);
        Assert.assertEquals((Integer) 1, eval);
    }

    @Test // "{hello}{hello}hello // hello
    public void simpleTemplateDouble() throws Exception {
        Template<String> str = new Template<>(){};
        str.setParsedJson(convert(str.type, "{hello}{hello}hello"));
        o.put("hello", "eval-value");
        String eval = str.eval(o);
        Assert.assertEquals("eval-valueeval-valuehello", eval);
        Assert.assertNotNull(str.toString());
    }

    @Test // "{hello}hello{hello}hello // hello
    public void simpleTemplateMixed() throws Exception {
        Template<String> str = new Template<>(){};
        str.setParsedJson(convert(str.type, "{hello}hello{hello}hello"));
        o.put("hello", "eval-value");
        String eval = str.eval(o);
        Assert.assertEquals("eval-valuehelloeval-valuehello", eval);
        Assert.assertNotNull(str.toString());
    }

    @Test // "[1,2,\"{template-int}\"]" // template-int
    public void simpleTemplateNestedInList() throws Exception {
        Template<List<Integer>> str = new Template<>(){};
        str.setParsedJson(convert(str.type, mapper.readValue("[1,2,\"{template-int}\"]", List.class)));
        o.put("template-int",3);
        List<Integer> eval = str.eval(o);
        Assert.assertEquals((Integer) 1, eval.get(0));
        Assert.assertEquals((Integer) 2, eval.get(1));
        Assert.assertEquals((Integer) 3, eval.get(2));
    }

    @Test // "{\"1\": 2, \"other\": \"{template-int}\"}" // template-int
    public void simpleTemplateNestedInMap() throws Exception {
        Template<Map<String, Integer>> str = new Template<>(){};
        str.setParsedJson(convert(str.type, mapper.readValue("{\"1\": 2, \"other\": \"{template-int}\"}", Map.class)));
        o.put("template-int",3);
        Map<String, Integer> eval = str.eval(o);
        Assert.assertEquals((Integer) 2, eval.get("1"));
        Assert.assertEquals((Integer) 3, eval.get("other"));
    }

    // == Bad

    @Test(expected = TemplateException.class)
    public void simpleTemplateMissingKey() throws Exception {
        Template<String> str = new Template<>(){};
        str.setParsedJson(convert(str.type, "{hello}"));
        str.eval(o);
    }

    @Test(expected = TemplateException.class)
    public void simpleTemplateBadKeyType() throws Exception {
        Template<Map<String, Integer>> str = new Template<>(){};
        str.setParsedJson(convert(str.type, mapper.readValue("{\"other\": \"{template-int}\"}", Map.class)));
        o.put("template-int","3b");
        Map<String, Integer> eval = str.eval(o);
        Assert.assertEquals((Integer) 3, eval.get("other"));
    }

    // == Get keys

    @Test // "{hello}{ok2}" -> hello, ok2
    public void simpleTemplateGetKeys() throws ValidationException {
        Collection<String> expected = Set.of("hello", "ok2");
        Template<String> str = new Template<>(){};
        str.setParsedJson(convert(str.type, "{hello}{ok2}"));
        Assert.assertEquals((Integer) 2, (Integer) str.getKeysInTemplate(o).size());
        str.getKeysInTemplate(o).forEach(key -> Assert.assertTrue(expected.contains(key)));
        expected.forEach(key -> Assert.assertTrue(str.getKeysInTemplate(o).contains(key)));
    }

    @Test // "[1,2,\"{template-int}\"]" -> template-int
    public void simpleTemplateNestedInListGetKeys() throws ValidationException, IOException {
        Collection<String> expected = Set.of("template-int");
        Template<List<Integer>> str = new Template<>(){};
        str.setParsedJson(convert(str.type, mapper.readValue("[1,2,\"{template-int}\"]", List.class)));
        Assert.assertEquals((Integer) 1, (Integer) str.getKeysInTemplate(o).size());
        str.getKeysInTemplate(o).forEach(key -> Assert.assertTrue(expected.contains(key)));
        expected.forEach(key -> Assert.assertTrue(str.getKeysInTemplate(o).contains(key)));
    }

    @Test // "{\"1\": 2, \"other\": \"{template-int}\"}" -> template-int
    public void simpleTemplateNestedInMapGetKeys() throws ValidationException, IOException {
        Collection<String> expected = Set.of("template-int");
        Template<Map<String, Integer>> str = new Template<>(){};
        str.setParsedJson(convert(str.type, mapper.readValue("{\"1\": 2, \"other\": \"{template-int}\"}", Map.class)));
        Assert.assertEquals((Integer) 1, (Integer) str.getKeysInTemplate(o).size());
        str.getKeysInTemplate(o).forEach(key -> Assert.assertTrue(expected.contains(key)));
        expected.forEach(key -> Assert.assertTrue(str.getKeysInTemplate(o).contains(key)));
    }




//    // ===============
//    // FM Lookup
//    // ===============

//    @Test(expected = TemplateException.class)
//    public void badConversionTest() {
//        class TemplateNotImplemented implements Serializable {}
//        Template<String> str = new Template<>(){};
//        str.setParsedJson(new TemplateNotImplemented());
//        str.eval(o);
//    }
//
//
//
//
//
//
    @Test(expected = TemplateException.class)
    public void nestedKeyLookupFail() throws Exception {
        Template<String> str = new Template<>(){};
        str.setParsedJson(convert(str.type, mapper.readValue("\"{list}\"}", String.class)));
        o.put("list", List.of("hello"));
        String evaled = str.eval(o);
        System.out.println(evaled);
    }

//    @Test
//    public void nestedKeyLookup() throws Exception {
//        Template<String> str = new Template<>(){};
//        str.setParsedJson(convert(str.type, mapper.readValue("\"@{list}[0]\"}", String.class)));
//        o.put("list", List.of("hello"));
//        o.put("hello", "1");
//        String eval = str.eval(o);
//        Assert.assertEquals("1", eval);
//    }
//
//    @Test(expected = TemplateException.class)
//    public void outOfBoundsArrayLookup() throws Exception {
//        Template<String> str = new Template<>(){};
//        str.setParsedJson(convert(str.type, mapper.readValue("\"@{list}[1]\"}", String.class)));
//        o.put("list", List.of("hello"));
//        str.eval(o);
//    }
//
//    @Test
//    public void arrayNegativeLookup() throws Exception {
//        Template<String> str = new Template<>(){};
//        str.setParsedJson(convert(str.type, mapper.readValue("\"{list}[-1]\"}", String.class)));
//        o.put("list", List.of("hello", "world"));
//        String eval = str.eval(o);
//        Assert.assertEquals("world", eval);
//    }
//
    @Test
    public void nestedMapEvalTest() throws Exception {
        Template<Map<String, Integer>> str = new Template<>(){};
        str.setParsedJson(convert(str.type, mapper.readValue("{\"other\": \"{{template-int}}\"}", Map.class)));
        o.put("template-int","3b");
        o.put("3b",3);
        Map<String, Integer> eval = str.eval(o);
        Assert.assertEquals((Integer) 3, eval.get("other"));
    }
}