package scraper.core.template;

import org.junit.jupiter.api.Test;
import scraper.api.ValidationException;
import scraper.api.T;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static scraper.util.TemplateUtil.parseTemplate;

public class TemplateEqualityTest {
    @Test // constants
    public void simpleTemplateNoKeys() {
        T<String> t1 = new T<>(){};
        t1.setTerm(parseTemplate("hello", t1));

        T<String> t2 = new T<>(){};
        t2.setTerm(parseTemplate("hello", t2));

        assertEquals(t1, t2);
    }

    @Test // constants
    public void simpleListTemplateNoKeys() throws ValidationException {
        T<List<String>> t1 = new T<>(){};
        t1.setTerm(parseTemplate(List.of("hello"), t1));

        T<List<String>> t2 = new T<>(){};
        t2.setTerm(parseTemplate(List.of("hello"), t2));

        assertEquals(t1, t2);
    }

    @Test // constants
    public void simpleMapTemplateNoKeys() throws ValidationException {
        T<Map<String, String>> t1 = new T<>(){};
        t1.setTerm(parseTemplate(Map.of(), t1));

        T<Map<String, String>> t2 = new T<>(){};
        t2.setTerm(parseTemplate(Map.of(), t2));

        assertEquals(t1, t2);
    }

    @Test // same template but different types
    public void sameTemplateDifferentTypes() throws ValidationException {
        T<Map<String, Integer>> t1 = new T<>(){};
        t1.setTerm(parseTemplate(Map.of(), t1));

        T<Map<String, String>> t2 = new T<>(){};
        t2.setTerm(parseTemplate(Map.of(), t2));

        assertNotEquals(t1, t2);
    }

    @Test // same template but different types
    public void differentTemplateSameTypes() throws ValidationException {
        T<Map<String, String>> t1 = new T<>(){};
        t1.setTerm(parseTemplate(Map.of("oh", "ok"), t1));

        T<Map<String, String>> t2 = new T<>(){};
        t2.setTerm(parseTemplate(Map.of(), t2));

        assertNotEquals(t1, t2);
    }

    @Test // same template same types
    public void sameTemplateSameTypes() throws ValidationException {
        T<Map<String, String>> t1 = new T<>(){};
        t1.setTerm(parseTemplate(Map.of("oh", "ok"), t1));

        T<Map<String, String>> t2 = new T<>(){};
        t2.setTerm(parseTemplate(Map.of("oh", "ok"), t2));

        assertEquals(t1, t2);
    }
}