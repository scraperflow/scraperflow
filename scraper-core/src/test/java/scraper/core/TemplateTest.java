package scraper.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scraper.api.exceptions.TemplateException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.reflect.T;
import scraper.util.NodeUtil;

import java.io.IOException;
import java.util.*;

import static scraper.util.NodeUtil.convert;

public class TemplateTest {

    private final static FlowMap o = new FlowMapImpl(UUID.randomUUID());
    private final static ObjectMapper mapper = new ObjectMapper();

    @Before
    public void clean() { o.clear(); }

    // ===============
    // SIMPLE TEMPLATE
    // ===============

    // == Eval

    @Test // "hello"
    public void simpleTemplateNoKeys() {
        T<String> simpleString = new T<>(){};
        simpleString.setParsedJson("hello");
        String eval = o.eval(simpleString);
        Assert.assertEquals("hello", eval);
        Assert.assertNotNull(simpleString.toString());
    }

    @Test // ""
    public void simpleTEmpty() throws ValidationException {
        T<String> str = new T<>(){};
        str.setParsedJson(convert(str, ""));
        Assert.assertEquals("", o.eval(str));
    }

    @Test // "{hello}" // hello
    public void simpleTSingle() throws Exception {
        T<String> str = new T<>(){};
        str.setParsedJson(convert(str, "{hello}"));
        o.put("hello", "eval-value");
        String eval = o.eval(str);
        Assert.assertEquals("eval-value", eval);
    }

    @Test // "{int}" // hello
    public void simpleTSingleInt() throws Exception {
        T<Integer> str = new T<>(){};
        str.setParsedJson(convert(str, "{int}"));
        o.put("int", 1);
        Integer eval = o.eval(str);
        Assert.assertEquals((Integer) 1, eval);
    }

    @Test // "{hello}{hello}hello // hello
    public void simpleTDouble() throws Exception {
        T<String> str = new T<>(){};
        str.setParsedJson(convert(str, "{hello}{hello}hello"));
        o.put("hello", "eval-value");
        String eval = o.eval(str);
        Assert.assertEquals("eval-valueeval-valuehello", eval);
        Assert.assertNotNull(str.toString());
    }

    @Test // "{hello}hello{hello}hello // hello
    public void simpleTMixed() throws Exception {
        T<String> str = new T<>(){};
        str.setParsedJson(convert(str, "{hello}hello{hello}hello"));
        o.put("hello", "eval-value");
        String eval = o.eval(str);
        Assert.assertEquals("eval-valuehelloeval-valuehello", eval);
        Assert.assertNotNull(str.toString());
    }

    @Test // "[1,2,\"{template-int}\"]" // template-int
    public void simpleTNestedInList() throws Exception {
        T<List<Integer>> str = new T<>(){};
        str.setParsedJson(convert(str, mapper.readValue("[1,2,\"{template-int}\"]", List.class)));
        o.put("template-int",3);
        List<Integer> eval = o.eval(str);
        Assert.assertEquals((Integer) 1, eval.get(0));
        Assert.assertEquals((Integer) 2, eval.get(1));
        Assert.assertEquals((Integer) 3, eval.get(2));
    }

    @Test // "{\"1\": 2, \"other\": \"{template-int}\"}" // template-int
    public void simpleTNestedInMap() throws Exception {
        T<Map<String, Integer>> str = new T<>(){};
        str.setParsedJson(convert(str, mapper.readValue("{\"1\": 2, \"other\": \"{template-int}\"}", Map.class)));
        o.put("template-int",3);
        Map<String, Integer> eval = o.eval(str);
        Assert.assertEquals((Integer) 2, eval.get("1"));
        Assert.assertEquals((Integer) 3, eval.get("other"));
    }

    // == Bad

    @Test(expected = TemplateException.class)
    public void simpleTMissingKey() throws Exception {
        T<String> str = new T<>(){};
        str.setParsedJson(convert(str, "{hello}"));
        o.eval(str);
    }

    @Test(expected = TemplateException.class)
    public void simpleTBadKeyType() throws Exception {
        T<Map<String, Integer>> str = new T<>(){};
        str.setParsedJson(convert(str, mapper.readValue("{\"other\": \"{template-int}\"}", Map.class)));
        o.put("template-int","3b");
        Map<String, Integer> eval = o.eval(str);
        Assert.assertEquals((Integer) 3, eval.get("other"));
    }

    // == Get keys

    @Test // "{hello}{ok2}" -> hello, ok2
    public void simpleTGetKeys() throws ValidationException {
        Collection<String> expected = Set.of("hello", "ok2");
        T<String> str = new T<>(){};
        str.setParsedJson(convert(str, "{hello}{ok2}"));
        Assert.assertEquals((Integer) 2, (Integer) Template.getKeysInTemplate(str, o).size());
        Template.getKeysInTemplate(str, o).forEach(key -> Assert.assertTrue(expected.contains(key)));
        expected.forEach(key -> Assert.assertTrue(Template.getKeysInTemplate(str, o).contains(key)));
    }

    @Test // "[1,2,\"{template-int}\"]" -> template-int
    public void simpleTNestedInListGetKeys() throws ValidationException, IOException {
        Collection<String> expected = Set.of("template-int");
        T<List<Integer>> str = new T<>(){};
        str.setParsedJson(convert(str, mapper.readValue("[1,2,\"{template-int}\"]", List.class)));
        Assert.assertEquals((Integer) 1, (Integer) Template.getKeysInTemplate(str, o).size());
        Template.getKeysInTemplate(str, o).forEach(key -> Assert.assertTrue(expected.contains(key)));
        expected.forEach(key -> Assert.assertTrue(Template.getKeysInTemplate(str, o).contains(key)));
    }

    @Test // "{\"1\": 2, \"other\": \"{template-int}\"}" -> template-int
    public void simpleTNestedInMapGetKeys() throws ValidationException, IOException {
        Collection<String> expected = Set.of("template-int");
        T<Map<String, Integer>> str = new T<>(){};
        str.setParsedJson(convert(str, mapper.readValue("{\"1\": 2, \"other\": \"{template-int}\"}", Map.class)));
        Assert.assertEquals((Integer) 1, (Integer) Template.getKeysInTemplate(str, o).size());
        Template.getKeysInTemplate(str, o).forEach(key -> Assert.assertTrue(expected.contains(key)));
        expected.forEach(key -> Assert.assertTrue(Template.getKeysInTemplate(str, o).contains(key)));
    }




//    // ===============
//    // FM Lookup
//    // ===============

//    @Test(expected = TException.class)
//    public void badConversionTest() {
//        class TNotImplemented implements Serializable {}
//        T<String> str = new T<>(){};
//        str.setParsedJson(new TNotImplemented());
//        o.eval(str);
//    }
//
//
//
//
//
//
    @Test(expected = TemplateException.class)
    public void nestedKeyLookupFail() throws Exception {
        T<String> str = new T<>(){};
        str.setParsedJson(convert(str, mapper.readValue("\"{list}\"}", String.class)));
        o.put("list", List.of("hello"));
        String evaled = o.eval(str);
        System.out.println(evaled);
    }

//    @Test
//    public void nestedKeyLookup() throws Exception {
//        T<String> str = new T<>(){};
//        str.setParsedJson(convert(str, mapper.readValue("\"@{list}[0]\"}", String.class)));
//        o.put("list", List.of("hello"));
//        o.put("hello", "1");
//        String eval = o.eval(str);
//        Assert.assertEquals("1", eval);
//    }
//
//    @Test(expected = TException.class)
//    public void outOfBoundsArrayLookup() throws Exception {
//        T<String> str = new T<>(){};
//        str.setParsedJson(convert(str, mapper.readValue("\"@{list}[1]\"}", String.class)));
//        o.put("list", List.of("hello"));
//        o.eval(str);
//    }
//
//    @Test
//    public void arrayNegativeLookup() throws Exception {
//        T<String> str = new T<>(){};
//        str.setParsedJson(convert(str, mapper.readValue("\"{list}[-1]\"}", String.class)));
//        o.put("list", List.of("hello", "world"));
//        String eval = o.eval(str);
//        Assert.assertEquals("world", eval);
//    }
//
    @Test
    public void nestedMapEvalTest() throws Exception {
        T<Map<String, Integer>> str = new T<>(){};
        str.setParsedJson(convert(str, mapper.readValue("{\"other\": \"{{template-int}}\"}", Map.class)));
        o.put("template-int","3b");
        o.put("3b",3);
        Map<String, Integer> eval = o.eval(str);
        Assert.assertEquals((Integer) 3, eval.get("other"));
    }
}