package scraper.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scraper.api.exceptions.NodeException;
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

    @Test
    public void evalStringTest() throws NodeException {
        Template<String> simpleString = new Template<>(){};
        simpleString.setTemplate("hello");
        String eval = simpleString.eval(o);
        Assert.assertEquals("hello", eval);

        Assert.assertNotNull(simpleString.toString());
    }

    @Test
    public void templateStringDirectTest() throws NodeException {
        TemplateString<String> template = new TemplateString<>(Map.of(), Map.of(), String.class);
        Assert.assertEquals("", template.eval(o));
        Assert.assertEquals(template.getType(), String.class);
    }

    @Test(expected = TemplateException.class)
    public void evalStringTemplateFailTest() throws NodeException, ValidationException {
        Template<String> str = new Template<>(){};
        str.setTemplate(convert(str.type, "{hello}"));
        str.eval(o);
    }

    @Test
    public void evalStringTemplateTest() throws NodeException, ValidationException {
        Template<String> str = new Template<>(){};
        str.setTemplate(convert(str.type, "{hello}"));
        o.put("hello", "eval-value");
        String eval = str.eval(o);
        Assert.assertEquals("eval-value", eval);
    }

    @Test
    public void doubleStringTemplate() throws NodeException, ValidationException {
        Template<String> str = new Template<>(){};
        str.setTemplate(convert(str.type, "{hello}{hello}hello"));
        o.put("hello", "eval-value");
        String eval = str.eval(o);
        Assert.assertEquals("eval-valueeval-valuehello", eval);
        Assert.assertNotNull(str.toString());
    }

    @Test
    public void evalListTemplateTest() throws NodeException, ValidationException, IOException {
        Template<List<Integer>> str = new Template<>(){};
        str.setTemplate(convert(str.type, mapper.readValue("[1,2,\"{template-int}\"]", List.class)));
        o.put("template-int",3);
        List<Integer> eval = str.eval(o);
        Assert.assertEquals((Integer) 1, eval.get(0));
        Assert.assertEquals((Integer) 2, eval.get(1));
        Assert.assertEquals((Integer) 3, eval.get(2));
    }

    @Test
    public void evalMapTemplateTest() throws NodeException, ValidationException, IOException {
        Template<Map<String, Integer>> str = new Template<>(){};
        str.setTemplate(convert(str.type, mapper.readValue("{\"1\": 2, \"other\": \"{template-int}\"}", Map.class)));
        o.put("template-int",3);
        Map<String, Integer> eval = str.eval(o);
        Assert.assertEquals((Integer) 2, eval.get("1"));
        Assert.assertEquals((Integer) 3, eval.get("other"));
    }

    @Test
    public void getKeysForStringTemplateTest() throws ValidationException {
        Collection<String> expected = Set.of("hello", "ok2");
        Template<String> str = new Template<>(){};
        str.setTemplate(convert(str.type, "{hello}{ok2}"));
        Assert.assertEquals((Integer) 2, (Integer) str.getKeysInTemplate().size());
        str.getKeysInTemplate().forEach(key -> Assert.assertTrue(expected.contains(key)));
        expected.forEach(key -> Assert.assertTrue(str.getKeysInTemplate().contains(key)));
    }

    @Test
    public void getKeysForListTemplateTest() throws ValidationException, IOException {
        Collection<String> expected = Set.of("template-int");
        Template<List<Integer>> str = new Template<>(){};
        str.setTemplate(convert(str.type, mapper.readValue("[1,2,\"{template-int}\"]", List.class)));
        Assert.assertEquals((Integer) 1, (Integer) str.getKeysInTemplate().size());
        str.getKeysInTemplate().forEach(key -> Assert.assertTrue(expected.contains(key)));
        expected.forEach(key -> Assert.assertTrue(str.getKeysInTemplate().contains(key)));
    }

    @Test
    public void getKeysMapTemplateTest() throws ValidationException, IOException {
        Collection<String> expected = Set.of("template-int");
        Template<Map<String, Integer>> str = new Template<>(){};
        str.setTemplate(convert(str.type, mapper.readValue("{\"1\": 2, \"other\": \"{template-int}\"}", Map.class)));
        Assert.assertEquals((Integer) 1, (Integer) str.getKeysInTemplate().size());
        str.getKeysInTemplate().forEach(key -> Assert.assertTrue(expected.contains(key)));
        expected.forEach(key -> Assert.assertTrue(str.getKeysInTemplate().contains(key)));
    }
}