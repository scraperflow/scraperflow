package scraper.plugins.core.typechecker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scraper.api.exceptions.TemplateException;
import scraper.api.exceptions.ValidationException;
import scraper.api.template.T;

import java.util.List;
import java.util.Map;

import static scraper.util.TemplateUtil.parseTemplate;

public class TypeCheckerTest {

    TypeChecker check = new TypeChecker();

    @Before
    public void resetChecker() { check = new TypeChecker(); }

    @Test
    public void simpleConstantString() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("hello", t));
        check.typeTemplate(t);
    }

    // fails at parsing, no need for type checking
    @Test(expected = Exception.class)
    public void simpleConstantStringFail() throws ValidationException {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate(100, t));
        check.typeTemplate(t);
    }

    @Test(expected = Exception.class)
    public void simpleConstantListFail() throws ValidationException {
        T<List<String>> t = new T<>(){};
        t.setTerm(parseTemplate(List.of("hello", 100), t));
        check.typeTemplate(t);
    }

    @Test
    public void simpleConstantMapOk() throws ValidationException {
        T<Map<String, Integer>> t = new T<>(){};
        // this is ok, since a String to Class converter is used for "200"
        t.setTerm(parseTemplate(Map.of("hello", 100, "noo", "200"), t));
        check.typeTemplate(t);
    }

    @Test(expected = Exception.class)
    public void simpleConstantMapFail() throws ValidationException {
        T<Map<String, String>> t = new T<>(){};
        // this is not ok, and fails at parsing
        t.setTerm(parseTemplate(Map.of("hello", 100, "noo", "200"), t));
        check.typeTemplate(t);
    }


    @Test(expected = Exception.class)
    public void simpleConstantStringFail2() throws ValidationException {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate(List.of("not a string"), t));
        check.typeTemplate(t);
    }

    @Test
    public void simpleConstantListString() throws ValidationException {
        T<List<String>> t = new T<>(){};
        t.setTerm(parseTemplate(List.of("hello", "world"), t));
        check.typeTemplate(t);
    }





    @Test(expected = TemplateException.class)
    public void simpleKeyLookupEmptyEnv() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{A}", t));
        check.typeTemplate(t);
    }


    @Test
    public void simpleKeyLookup() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{A}", t));

        {
            T<String> known = new T<>(){};
            known.setTerm(parseTemplate("A", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }


    @Test
    public void nestedKeyLookup() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{{A}}", t));

        { // A :: String
            T<String> known = new T<>(){};
            known.setTerm(parseTemplate("A", known));
            check.add(known.getTerm());
        }
        { // {A} :: String
            T<String> known = new T<>(){};
            known.setTerm(parseTemplate("{A}", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }

    @Test(expected = TemplateException.class)
    public void nestedKeyLookupFail() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{{A}}", t));

        { // A :: String is missing
            T<String> known = new T<>(){};
            known.setTerm(parseTemplate("{A}", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }

    @Test(expected = TemplateException.class)
    public void nestedKeyLookupFail2() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{{A}}", t));

        { // {A} :: String is missing
            T<String> known = new T<>(){};
            known.setTerm(parseTemplate("A", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }

    @Test
    public void nestedKeyLookupFailConvertToInt() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{{A}}", t));

        { // A :: Integer
            T<Integer> known = new T<>(){};
            known.setTerm(parseTemplate("A", known));
            check.add(known.getTerm());
        }
        { // {A} :: String
            T<String> known = new T<>(){};
            known.setTerm(parseTemplate("{A}", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }

    @Test
    public void nestedKeyLookupConvertToInt() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{{A}}", t));

        { // A :: String
            T<String> known = new T<>(){};
            known.setTerm(parseTemplate("A", known));
            check.add(known.getTerm());
        }
        { // {A} :: Integer
            T<Integer> known = new T<>(){};
            known.setTerm(parseTemplate("{A}", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }


    @Test
    public void keyLookupConvertLookupType() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{{map}@key}", t));

        { // {map} :: Map<String, String>
            T<Map<String, Integer>> known = new T<>(){};
            known.setTerm(parseTemplate("map", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }

    @Test
    public void keyLookupType() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{{map}@key}", t));

        { // map :: Map<String, String>
            T<Map<String, String>> known = new T<>(){};
            known.setTerm(parseTemplate("map", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }


    @Test
    public void simpleKeyLookupInList() throws ValidationException {
        T<List<String>> t = new T<>(){};
        t.setTerm(parseTemplate(List.of("{A}"), t));

        { // A :: String
            T<String> known = new T<>(){};
            known.setTerm(parseTemplate("A", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }

    @Test
    public void simpleKeyLookupInListConvertType() throws ValidationException {
        T<List<String>> t = new T<>(){};
        t.setTerm(parseTemplate(List.of("{A}"), t));

        { // A :: Integer
            T<Integer> known = new T<>(){};
            known.setTerm(parseTemplate("A", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }

    @Test(expected = TemplateException.class)
    public void simpleKeyLookupInListFail() throws ValidationException {
        T<List<String>> t = new T<>(){};
        t.setTerm(parseTemplate(List.of("{A}"), t));
        check.typeTemplate(t);
    }

    @Test(expected = TemplateException.class)
    public void simpleKeyLookupInListFail2() throws ValidationException {
        T<List<String>> t = new T<>(){};
        t.setTerm(parseTemplate(List.of("Constant!", "{A}", "{not-ok}"), t));

        { // A :: String
            T<String> known = new T<>(){};
            known.setTerm(parseTemplate("A", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }

    @Test(expected = TemplateException.class)
    public void simpleKeyLookupInListFail3() throws ValidationException {
        T<List<Integer>> t = new T<>(){};
        t.setTerm(parseTemplate(List.of(42, "{A}", "{not-ok}"), t));

        { // A :: Integer
            T<Integer> known = new T<>(){};
            known.setTerm(parseTemplate("A", known));
            check.add(known.getTerm());
        }

        { // not-ok :: String
            T<String> known = new T<>(){};
            known.setTerm(parseTemplate("not-ok", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }

    @Test
    public void simpleConstantMap() throws ValidationException {
        T<Map<String, String>> t = new T<>(){};
        t.setTerm(parseTemplate(Map.of("ok", "hello"), t));
        check.typeTemplate(t);
    }

    @Test
    public void simpleMapWithTemplates() throws ValidationException {
        T<Map<String, String>> t = new T<>(){};
        t.setTerm(parseTemplate(Map.of("ok", "{hello}"), t));

        { // hello :: String
            T<String> known = new T<>(){};
            known.setTerm(parseTemplate("hello", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }

    @Test
    public void simpleMapLookup() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{{map}@key}", t));

        { // map :: Map<String, String>
            T<Map<String, String>> known = new T<>(){};
            known.setTerm(parseTemplate("map", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }

    @Test
    public void simpleMapLookupConvertToInt() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{{map}@key}", t));

        { // map :: Map<String, Integer>
            T<Map<String, Integer>> known = new T<>(){};
            known.setTerm(parseTemplate("map", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }

    @Test
    public void simpleNestedMapLookup() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{{{map}@key}@another}", t));

        { // map :: Map<String, Map<String, String>>
            T<Map<String, Map<String, String>>> known = new T<>(){};
            known.setTerm(parseTemplate("map", known));
            check.add(known.getTerm());
        }
        check.typeTemplate(t);
    }

    @Test(expected = TemplateException.class)
    public void simpleNestedMapLookupFail() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{{{map}@key}@another}", t));

        { // map :: Map<String, Integer>
            T<Map<String, Integer>> known = new T<>(){};
            known.setTerm(parseTemplate("map", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }

    @Test
    public void simpleListLookup() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{{list}}[0]", t));

        { // list :: List<String>
            T<List<String>> known = new T<>(){};
            known.setTerm(parseTemplate("list", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }


    @Test
    public void simpleListLookupConvertIntToString() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{{list}}[0]", t));

        { // list :: List<Integer>
            T<List<Integer>> known = new T<>(){};
            known.setTerm(parseTemplate("list", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }

    @Test
    public void simpleConcat() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{a}{b}", t));

        { // a :: String
            T<String> known = new T<>(){};
            known.setTerm(parseTemplate("a", known));
            check.add(known.getTerm());
        }
        { // b :: String
            T<String> known = new T<>(){};
            known.setTerm(parseTemplate("b", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }

    @Test(expected = TemplateException.class)
    public void simpleConcatFail() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{a}{b}", t));

        { // a :: String
            T<String> known = new T<>(){};
            known.setTerm(parseTemplate("a", known));
            check.add(known.getTerm());
        }
        { // b :: List<String>
            T<List<String>> known = new T<>(){};
            known.setTerm(parseTemplate("b", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }


    @Test
    public <X> void keyTypeLookupWithKnown() {
        T<X> t = new T<>(){};
        t.setTerm(parseTemplate("{A}", t));

        {
            T<String> known = new T<>(){};
            known.setTerm(parseTemplate("A", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }

    @Test
    public <X> void keyTypeLookupComposition() {
        T<X> t = new T<>(){};
        t.setTerm(parseTemplate("{A}", t));

        {
            T<X> known = new T<>(){};
            known.setTerm(parseTemplate("A", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }

    @Test
    public <X> void keyLookupWithUnknown() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{A}", t));

        {
            T<X> known = new T<>(){};
            known.setTerm(parseTemplate("A", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
    }

    @Test
    public <X> void listCapture() throws ValidationException {
        T<X> t = new T<>(){};
        t.setTerm(parseTemplate(List.of("{A}"), t));

        {
            T<String> known = new T<>(){};
            known.setTerm(parseTemplate("A", known));
            check.add(known.getTerm());
        }

        check.typeTemplate(t);
        Assert.assertEquals(
                "java.util.List<java.lang.String>",
                check.captures.get("X").get().getTypeName()
        );
    }

    @Test
    public <X> void replaceTypeVariableWithStaticType() {
        T<X> t = new T<>(){};
        t.setTerm(parseTemplate("static", t));

        check.typeTemplate(t);
        check.add(t.getTerm());
        Assert.assertEquals(
                "java.lang.String",
                check.env.get(t.getTerm()).getTypeName()
        );

    }
}
