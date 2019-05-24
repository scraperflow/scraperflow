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
import java.io.Serializable;
import java.util.*;

import static scraper.util.NodeUtil.convert;

public class TemplateTest {

    private final static FlowMap o = NodeUtil.flowOf(new HashMap<>());
    private final static ObjectMapper mapper = new ObjectMapper();

    @Before
    public void clean() { o.clear(); }

    @Test
    public void evalStringTest() {
        Template<String> simpleString = new Template<>(){};
        simpleString.setParsedJson("hello");
        String eval = simpleString.eval(o);
        Assert.assertEquals("hello", eval);

        Assert.assertNotNull(simpleString.toString());
    }

    @Test
    public void templateStringDirectTest() {
        TemplateString<String> template = new TemplateString<>(Map.of(), Map.of(), String.class);
        Assert.assertEquals("", template.eval(o));
        Assert.assertEquals(template.getType(), String.class);
    }

    @Test(expected = TemplateException.class)
    public void evalStringTemplateFailTest() throws Exception {
        Template<String> str = new Template<>(){};
        str.setParsedJson(convert(str.type, "{hello}"));
        str.eval(o);
    }

    @Test
    public void evalStringTemplateTest() throws Exception {
        Template<String> str = new Template<>(){};
        str.setParsedJson(convert(str.type, "{hello}"));
        o.put("hello", "eval-value");
        String eval = str.eval(o);
        Assert.assertEquals("eval-value", eval);
    }

    @Test
    public void doubleStringTemplate() throws Exception {
        Template<String> str = new Template<>(){};
        str.setParsedJson(convert(str.type, "{hello}{hello}hello"));
        o.put("hello", "eval-value");
        String eval = str.eval(o);
        Assert.assertEquals("eval-valueeval-valuehello", eval);
        Assert.assertNotNull(str.toString());
    }

    @Test
    public void mixedStringTemplate() throws Exception {
        Template<String> str = new Template<>(){};
        str.setParsedJson(convert(str.type, "{hello}hello{hello}hello"));
        o.put("hello", "eval-value");
        String eval = str.eval(o);
        Assert.assertEquals("eval-valuehelloeval-valuehello", eval);
        Assert.assertNotNull(str.toString());
    }

    @Test
    public void evalListTemplateTest() throws Exception {
        Template<List<Integer>> str = new Template<>(){};
        str.setParsedJson(convert(str.type, mapper.readValue("[1,2,\"{template-int}\"]", List.class)));
        o.put("template-int",3);
        List<Integer> eval = str.eval(o);
        Assert.assertEquals((Integer) 1, eval.get(0));
        Assert.assertEquals((Integer) 2, eval.get(1));
        Assert.assertEquals((Integer) 3, eval.get(2));
    }

    @Test
    public void evalMapTemplateTest() throws Exception {
        Template<Map<String, Integer>> str = new Template<>(){};
        str.setParsedJson(convert(str.type, mapper.readValue("{\"1\": 2, \"other\": \"{template-int}\"}", Map.class)));
        o.put("template-int",3);
        Map<String, Integer> eval = str.eval(o);
        Assert.assertEquals((Integer) 2, eval.get("1"));
        Assert.assertEquals((Integer) 3, eval.get("other"));
    }

    @Test
    public void getKeysForStringTemplateTest() throws ValidationException {
        Collection<String> expected = Set.of("hello", "ok2");
        Template<String> str = new Template<>(){};
        str.setParsedJson(convert(str.type, "{hello}{ok2}"));
        Assert.assertEquals((Integer) 2, (Integer) str.getKeysInTemplate().size());
        str.getKeysInTemplate().forEach(key -> Assert.assertTrue(expected.contains(key)));
        expected.forEach(key -> Assert.assertTrue(str.getKeysInTemplate().contains(key)));
    }

    @Test
    public void getKeysForListTemplateTest() throws ValidationException, IOException {
        Collection<String> expected = Set.of("template-int");
        Template<List<Integer>> str = new Template<>(){};
        str.setParsedJson(convert(str.type, mapper.readValue("[1,2,\"{template-int}\"]", List.class)));
        Assert.assertEquals((Integer) 1, (Integer) str.getKeysInTemplate().size());
        str.getKeysInTemplate().forEach(key -> Assert.assertTrue(expected.contains(key)));
        expected.forEach(key -> Assert.assertTrue(str.getKeysInTemplate().contains(key)));
    }

    @Test
    public void getKeysMapTemplateTest() throws ValidationException, IOException {
        Collection<String> expected = Set.of("template-int");
        Template<Map<String, Integer>> str = new Template<>(){};
        str.setParsedJson(convert(str.type, mapper.readValue("{\"1\": 2, \"other\": \"{template-int}\"}", Map.class)));
        Assert.assertEquals((Integer) 1, (Integer) str.getKeysInTemplate().size());
        str.getKeysInTemplate().forEach(key -> Assert.assertTrue(expected.contains(key)));
        expected.forEach(key -> Assert.assertTrue(str.getKeysInTemplate().contains(key)));
    }

    @Test(expected = TemplateException.class)
    public void badConversionTest() {
        class TemplateNotImplemented implements Serializable {}
        Template<String> str = new Template<>(){};
        str.setParsedJson(new TemplateNotImplemented());
        str.eval(o);
    }

    @Test(expected = TemplateException.class)
    public void evalToWrongTypeTest() throws Exception {
        Template<Map<String, Integer>> str = new Template<>(){};
        str.setParsedJson(convert(str.type, mapper.readValue("{\"other\": \"{template-int}\"}", Map.class)));
        o.put("template-int","3b");
        Map<String, Integer> eval = str.eval(o);
        Assert.assertEquals((Integer) 3, eval.get("other"));
    }

}