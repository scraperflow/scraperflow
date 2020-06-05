//package scraper.core.template;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import scraper.api.exceptions.TemplateException;
//import scraper.api.flow.FlowMap;
//import scraper.api.flow.impl.FlowMapImpl;
//import scraper.api.template.T;
//import scraper.api.template.Term;
//import scraper.util.TemplateUtil;
//
//import java.util.List;
//import java.util.Map;
//
//public class RecordExpTest {
//
//    private final static FlowMap o = new FlowMapImpl();
//
//    @Before public void clean() { o.clear(); }
//
//    @Test
//    public void simpleListRecordTest() {
//        o.output("L", List.of("str", 0));
//
//        {
//            String source = "{{L}}[0]";
//            Term<String> test = TemplateUtil.parseTemplate(source, new T<>() {});
//            String target = test.eval(o);
//            Assert.assertEquals("str", target);
//        }
//        {
//            String source = "{{L}}[1]";
//            Term<Integer> test = TemplateUtil.parseTemplate(source, new T<>() {});
//            Integer target = test.eval(o);
//            Assert.assertEquals((Integer) 0, target);
//        }
//    }
//
//    @Test(expected = TemplateException.class)
//    public void invalidListElementTypeTest() {
//        o.output("L", List.of("str", 0));
//
//        String source = "{{L}}[1]";
//        Term<String> test = TemplateUtil.parseTemplate(source, new T<>() {});
//        @SuppressWarnings("unused") // needed for cast
//         String eval = test.eval(o);
//    }
//
//    @Test
//    public void validListGenericElementTypeTest() {
//        o.output("L", List.of("str", Map.of("a", 100)));
//
//        String source = "{{L}}[1]";
//        Term<Map<String, Integer>> test = TemplateUtil.parseTemplate(source, new T<>() {});
//        Map<String, Integer> eval = test.eval(o);
//        Assert.assertEquals((Integer) 100, eval.get("a"));
//    }
//
//    @Test(expected = TemplateException.class)
//    public void invalidListGenericElementTypeTest() {
//        o.output("L", List.of("str", Map.of("a", 100)));
//
//        String source = "{{L}}[1]";
//        Term<List<Integer>> test = TemplateUtil.parseTemplate(source, new T<>() {});
//        @SuppressWarnings("unused") // needed for cast
//                List<Integer> eval = test.eval(o);
//    }
//
//
//    @Test
//    public void simpleMapRecordTest() {
//        o.output("L", Map.of("str", "0", "notstr", 42));
//
//        {
//            String source = "{{L}@str}";
//            Term<String> test = TemplateUtil.parseTemplate(source, new T<>() {});
//            String target = test.eval(o);
//            Assert.assertEquals("0", target);
//        }
//        {
//            String source = "{{L}@notstr}";
//            Term<Integer> test = TemplateUtil.parseTemplate(source, new T<>() {});
//            Integer target = test.eval(o);
//            Assert.assertEquals((Integer) 42, target);
//        }
//    }
//
//    @Test(expected = TemplateException.class)
//    public void invalidMapElementTypeTest() {
//        o.output("L", Map.of("str", "0", "notstr", 42));
//
//        String source = "{{L}@notstr}";
//        Term<String> test = TemplateUtil.parseTemplate(source, new T<>() {});
//        @SuppressWarnings("unused") // needed for cast
//        String eval = test.eval(o);
//    }
//
//    @Test
//    public void validMapGenericElementTypeTest() {
//        o.output("L", List.of("str", Map.of("a", 100)));
//        o.output("L", Map.of("str", "0", "notstr", List.of("asdasd", "200")));
//
//        String source = "{{L}@notstr}";
//        Term<List<String>> test = TemplateUtil.parseTemplate(source, new T<>() {});
//        List<String> eval = test.eval(o);
//        Assert.assertEquals("asdasd", eval.get(0));
//    }
//
//    @Test
//    public void validWildcardTemplateTest() {
//        o.output("L", List.of("str", Map.of("a", 100)));
//
//        String source = "{L}";
//        Term<List<?>> test = TemplateUtil.parseTemplate(source, new T<>() {});
//        List<?> eval = test.eval(o);
//        Assert.assertEquals(Map.of("a", 100), eval.get(1));
//        Assert.assertEquals("str", eval.get(0));
//    }
//
//    @Test
//    public void validSuperclassTemplateTest() {
//        o.output("L", List.of("str", Map.of("a", 100)));
//
//        String source = "{L}";
//        Term<List<Object>> test = TemplateUtil.parseTemplate(source, new T<>() {});
//        List<Object> eval = test.eval(o);
//        Assert.assertEquals(Map.of("a", 100), eval.get(1));
//        Assert.assertEquals("str", eval.get(0));
//    }
//}