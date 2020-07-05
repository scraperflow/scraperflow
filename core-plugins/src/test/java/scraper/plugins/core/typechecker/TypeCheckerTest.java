package scraper.plugins.core.typechecker;

import org.junit.jupiter.api.*;
import scraper.api.exceptions.TemplateException;
import scraper.api.exceptions.ValidationException;
import scraper.api.template.L;
import scraper.api.template.T;
import scraper.core.template.TemplateConstant;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static scraper.util.TemplateUtil.parseTemplate;

public class TypeCheckerTest {

    TypeChecker check = new TypeChecker();
    TypeEnvironment env = new TypeEnvironment();

    @BeforeEach
    public void resetChecker() { check = new TypeChecker(); env = new TypeEnvironment(); }

    @Test
    public void simpleConstantString() {
        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("hello", t));
        check.typeTemplate(env, t);
    }

    @Test
    public <A> void simpleConstantStringWithCapture() {
        T<A> t = new T<>(){};
        t.setTerm(parseTemplate("hello", t));
        check.typeTemplate(env, t);

        assertEquals(
                "java.lang.String",
                check.resolve("A").getTypeString()
        );
    }

    @Test
    public <A> void simpleConstantStringWithCaptureTwiceOk() {
        T<A> t = new T<>(){};
        t.setTerm(parseTemplate("hello", t));
        check.typeTemplate(env, t);

        T<A> t2 = new T<>(){};
        t2.setTerm(parseTemplate("hello2", t2));
        check.typeTemplate(env, t2);
    }

    @Test
    public <A,B> void simpleConstantWithCaptureTwiceDifferentOk() throws ValidationException {
        T<A> t = new T<>(){};
        t.setTerm(parseTemplate("hello", t));
        check.typeTemplate(env, t);

        T<B> t2 = new T<>(){};
        t2.setTerm(parseTemplate(List.of("i", "ok"), t2));
        check.typeTemplate(env, t2);
    }

    @Test
    public <A> void simpleConstantListWithCapture() throws ValidationException {
        T<A> t = new T<>(){};
        t.setTerm(parseTemplate(List.of("hello", "test"), t));
        check.typeTemplate(env, t);
    }

    @Test
    public <A> void simpleConstantListWithCaptureTwice() throws ValidationException {
        {
            T<A> t = new T<>(){};
            t.setTerm(parseTemplate(List.of("hello", "test"), t));
            check.typeTemplate(env, t);
        }

        {
            T<A> t = new T<>(){};
            t.setTerm(parseTemplate(List.of("hello", "test"), t));
            check.typeTemplate(env, t);
        }
    }

    @Test
    public <A> void simpleConstantListWithCaptureTwiceStringToIntFail() throws ValidationException {
        assertThrows(TemplateException.class,() -> {
            {
                T<A> t = new T<>(){};
                t.setTerm(parseTemplate(List.of(42, 21), t));
                check.typeTemplate(env, t);
            }

            {
                T<A> t = new T<>(){};
                t.setTerm(parseTemplate(List.of("Should fail"), t));
                check.typeTemplate(env, t);
            }
        });
    }

    @Test
    public <A, B> void simpleConstantListWithCaptureTwiceDifferentOk() throws ValidationException {
        {
            T<A> t = new T<>(){};
            t.setTerm(parseTemplate(List.of("hello", "test"), t));
            check.typeTemplate(env, t);
        }

        {
            T<B> t = new T<>(){};
            t.setTerm(parseTemplate(List.of(42, 42), t));
            check.typeTemplate(env, t);
        }
    }

    @Test
    public <A> void simpleConstantListWithCaptureFail2() throws ValidationException {
        assertThrows(TemplateException.class,() -> {
            {
                T<A> t = new T<>(){};
                t.setTerm(parseTemplate(List.of("hello", "test"), t));
                check.typeTemplate(env, t);
            }

            {
                T<List<A>> t = new T<>(){};
                t.setTerm(parseTemplate(List.of("hello", "diff"), t));
                check.typeTemplate(env, t);
            }
        });
    }

    @Test
    public <A> void simpleConstantListFail2() throws ValidationException {
        assertThrows(TemplateException.class,() -> {
            T<A> t = new T<>(){};
            t.setTerm(parseTemplate(List.of("hello", "test", List.of()), t));
            check.typeTemplate(env, t);
        });
    }

    // fails at parsing, no need for type checking
    @Test
    public void simpleConstantStringFail() throws ValidationException {
        assertThrows(Exception.class,() -> {
            T<String> t = new T<>(){};
            t.setTerm(parseTemplate(100, t));
            check.typeTemplate(env, t);
        });
    }

    @Test
    public void simpleConstantListFail() throws ValidationException {
        assertThrows(Exception.class,() -> {
            T<List<String>> t = new T<>(){};
            t.setTerm(parseTemplate(List.of("hello", 100), t));
            check.typeTemplate(env, t);
        });
    }

    @Test
    public void simpleConstantMapOk() throws ValidationException {
        T<Map<String, String>> t = new T<>(){};
        t.setTerm(parseTemplate(Map.of("hello", "ok", "world", "no"), t));
        check.typeTemplate(env, t);
    }

    @Test
    public void simpleConstantMapConvertOk() throws ValidationException {
        T<Map<String, Integer>> t = new T<>(){};
        // this is ok, since a String to Class converter is used for "200"
        t.setTerm(parseTemplate(Map.of("hello", 100, "noo", "200"), t));
        check.typeTemplate(env, t);
    }

    @Test
    public void simpleConstantMapFail() throws ValidationException {
        assertThrows(Exception.class,() -> {
            T<Map<String, String>> t = new T<>(){};
            // this is not ok, and fails at parsing
            t.setTerm(parseTemplate(Map.of("hello", 100, "noo", "200"), t));
            check.typeTemplate(env, t);
        });
    }


    @Test
    public void simpleConstantStringFail2() throws ValidationException {
        assertThrows(Exception.class,() -> {
            T<String> t = new T<>(){};
            t.setTerm(parseTemplate(List.of("not a string"), t));
            check.typeTemplate(env, t);
        });
    }

    @Test
    public void simpleConstantListString() throws ValidationException {
        T<List<String>> t = new T<>(){};
        t.setTerm(parseTemplate(List.of("hello", "world"), t));
        check.typeTemplate(env, t);
    }

    @Test
    public <A> void simpleConstantMapWithCapture() throws ValidationException {
        T<A> t = new T<>(){};
        t.setTerm(parseTemplate(Map.of("hello", "test"), t));
        check.typeTemplate(env, t);
    }

    @Test
    public <A> void simpleConstantMapWithCaptureTwice() throws ValidationException {
        {
            T<A> t = new T<>(){};
            t.setTerm(parseTemplate(Map.of("hello", "tst"), t));
            check.typeTemplate(env, t);
        }

        {
            T<A> t = new T<>(){};
            t.setTerm(parseTemplate(Map.of("helo", "test"), t));
            check.typeTemplate(env, t);
        }
    }

    @Test
    public <A> void simpleConstantMapWithCaptureTwiceFail() throws ValidationException {
        assertThrows(TemplateException.class,() -> {
            {
                T<A> t = new T<>(){};
                t.setTerm(parseTemplate(Map.of("hello", "test"), t));
                check.typeTemplate(env, t);
            }

            {
                T<A> t = new T<>(){};
                t.setTerm(parseTemplate(Map.of("hoo", 42), t));
                check.typeTemplate(env, t);
            }
        });
    }

    @Test
    public <A, B> void simpleConstantMapWithCaptureTwiceDifferentOk() throws ValidationException {
        {
            T<A> t = new T<>(){};
            t.setTerm(parseTemplate(Map.of("hello", "test"), t));
            check.typeTemplate(env, t);
        }

        {
            T<B> t = new T<>(){};
            t.setTerm(parseTemplate(Map.of("42", 42), t));
            check.typeTemplate(env, t);
        }
    }

    @Test
    public <A> void simpleConstantMapWithCaptureFail2() throws ValidationException {
        assertThrows(TemplateException.class,() -> {
            {
                T<A> t = new T<>(){};
                t.setTerm(parseTemplate(Map.of("hello", "test"), t));
                check.typeTemplate(env, t);
            }

            {
                T<Map<String, A>> t = new T<>(){};
                t.setTerm(parseTemplate(Map.of("hello", "diff"), t));
                check.typeTemplate(env, t);
            }
        });
    }

    @Test
    public void simpleAnyObject() throws ValidationException {
        {
            T<Object> t = new T<>(){};
            t.setTerm(parseTemplate("I'm object", t));
            check.typeTemplate(env, t);
        }
        {
            T<Object> t = new T<>(){};
            t.setTerm(parseTemplate(List.of("I'm object"), t));
            check.typeTemplate(env, t);
        }
        {
            T<Object> t = new T<>(){};
            t.setTerm(parseTemplate(Map.of("@","I'm object"), t));
            check.typeTemplate(env, t);
        }
    }

    @Test
    public void simpleKeyLookup() {
        add(new L<String>(){}, "A");

        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{A}", t));
        check.typeTemplate(env, t);
    }

    @Test
    public void simpleKeyLookupEmptyEnv() {
        assertThrows(TemplateException.class,() -> {
            T<String> t = new T<>(){};
            t.setTerm(parseTemplate("{A}", t));
            check.typeTemplate(env, t);
        });
    }

    @Test
    public void nestedKeyLookup() {
        { // A :: String
            T<String> known = new T<>(){};
            known.setTerm(parseTemplate("static string!", known));
            check.typeTemplate(env, known);
            add(new L<String>(){}, "A");
        }
        { // {A} :: String
            T<String> known = new T<>(){};
            known.setTerm(parseTemplate("{A}", known));
            check.typeTemplate(env, known);
            check.add(env, known.getTerm(), known);
        }

        { // {{A}} :: String
            T<String> t = new T<>(){};
            t.setTerm(parseTemplate("{{A}}", t));
            check.typeTemplate(env, t);
        }
    }

    @Test
    public void nestedKeyLookupFail() {
        assertThrows(TemplateException.class,() -> {
            { // A :: String is missing
                T<String> known = new T<>(){};
                known.setTerm(parseTemplate("{A}", known));
                // TODO this is not even possible I think with locations at the moment
                check.add(env, known.getTerm(), known);
            }

            {
                T<String> t = new T<>(){};
                t.setTerm(parseTemplate("{{A}}", t));
                check.typeTemplate(env, t);
            }
        });
    }


    @Test
    public void nestedKeyLookupFail2() {
        assertThrows(TemplateException.class,() -> {
            add(new L<String>(){}, "A");

            { // {A} :: String is missing
                T<String> t = new T<>(){};
                t.setTerm(parseTemplate("{{A}}", t));
                check.typeTemplate(env, t);
            }
        });
    }

    @Test
    public <X> void simpleKeyLookupTypeVar() {
        add(new L<String>(){}, "A");

        T<X> t = new T<>(){};
        t.setTerm(parseTemplate("{A}", t));
        check.typeTemplate(env, t);
        assertNotNull( check.captures.get("X") );
    }

    @Test
    public <X, Y> void sameKeyLookupTypeVar() {
        add(new L<String>(){}, "A");

        T<X> t = new T<>(){};
        t.setTerm(parseTemplate("{A}", t));

        T<Y> t2 = new T<>(){};
        t2.setTerm(parseTemplate("{A}", t2));

        check.typeTemplate(env, t);
        check.typeTemplate(env, t2);
    }

    @Test
    public <X> void keyLookupTypeVarFail() throws ValidationException {
        assertThrows(TemplateException.class,() -> {
            add(new L<String>(){}, "A");

            {
                T<X> t = new T<>(){}; // bind X to List<String>
                t.setTerm(parseTemplate(List.of("hello", "test"), t));
                check.typeTemplate(env, t);
            }

            {
                T<X> t = new T<>(){}; // but X evaluates to String
                t.setTerm(parseTemplate("{A}", t));
                check.typeTemplate(env, t);
            }
        });
    }

    @Test
    public void keyLookupObject() {
        add(new L<String>(){}, "A");

        T<Object> t = new T<>(){};
        t.setTerm(parseTemplate("{A}", t));
        check.typeTemplate(env, t);
    }


    @Test
    public void keyLookup()  {
        add(new L<Map<String, String>>(){}, "map");

        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{{map}@key}", t));
        check.typeTemplate(env, t);
    }


    @Test
    public void keyLookupType() {
        add(new L<Map<String, Integer>>(){}, "map");

        T<Integer> t = new T<>(){};
        t.setTerm(parseTemplate("{{map}@key}", t));
        check.typeTemplate(env, t);
    }


    @Test
    public void simpleKeyLookupInList() throws ValidationException {
        add(new L<String>(){}, "A");

        T<List<String>> t = new T<>(){};
        t.setTerm(parseTemplate(List.of("{A}"), t));

        check.typeTemplate(env, t);
    }

    @Test
    public void simpleKeyLookupInListFail() throws ValidationException {
        assertThrows(TemplateException.class,() -> {
            T<List<String>> t = new T<>(){};
            t.setTerm(parseTemplate(List.of("{A}"), t));
            check.typeTemplate(env, t);
        });
    }

    @Test
    public void simpleKeyLookupInListFail2() throws ValidationException {
        assertThrows(TemplateException.class,() -> {
            add(new L<String>(){}, "A");

            T<List<String>> t = new T<>(){};
            t.setTerm(parseTemplate(List.of("Constant!", "{A}", "{not-ok}"), t));

            check.typeTemplate(env, t);
        });
    }

    @Test
    public void simpleKeyLookupInListFail3() throws ValidationException {
        assertThrows(TemplateException.class,() -> {
            add(new L<Integer>(){}, "A");
            add(new L<String>(){}, "not-ok");

            T<List<Integer>> t = new T<>(){};
            t.setTerm(parseTemplate(List.of(42, "{A}", "{not-ok}"), t));

            check.typeTemplate(env, t);
        });
    }

    @Test
    public void simpleConstantMap() throws ValidationException {
        T<Map<String, String>> t = new T<>(){};
        t.setTerm(parseTemplate(Map.of("ok", "hello"), t));
        check.typeTemplate(env, t);
    }

    @Test
    public void simpleMapWithTemplates() throws ValidationException {
        add(new L<String>(){}, "hello");

        T<Map<String, String>> t = new T<>(){};
        t.setTerm(parseTemplate(Map.of("ok", "{hello}"), t));

        check.typeTemplate(env, t);
    }

    @Test
    public void simpleMapLookup() {
        add(new L<Map<String, String>>(){}, "map");

        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{{map}@key}", t));
        check.typeTemplate(env, t);
    }


    @Test
    public void simpleNestedMapLookup() {
        add(new L<Map<String, Map<String, String>>>(){}, "map");

        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{{{map}@key}@another}", t));
        check.typeTemplate(env, t);
    }

    @Test
    public void simpleNestedMapLookupFail() {
        assertThrows(TemplateException.class,() -> {
            add(new L<Map<String, Integer>>(){}, "map");

            T<String> t = new T<>(){};
            t.setTerm(parseTemplate("{{{map}@key}@another}", t));

            check.typeTemplate(env, t);
        });
    }

    @Test
    public void simpleListLookup() {
        add(new L<List<String>>(){}, "list");

        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{{list}}[0]", t));
        check.typeTemplate(env, t);
    }


    @Test
    public void simpleConcat() {
        add(new L<String>(){}, "a");
        add(new L<String>(){}, "b");

        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{a}{b}", t));

        check.typeTemplate(env, t);
    }

    @Test
    public void simpleConcat2() {
        add(new L<String>(){}, "a");
        add(new L<String>(){}, "b");

        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("very {a} hmm {b} string", t));

        check.typeTemplate(env, t);
    }

    @Test
    public void simpleConcatFail() {
        assertThrows(TemplateException.class,() -> {
            add(new L<String>(){}, "a");
            add(new L<List<String>>(){}, "b");
            T<String> t = new T<>(){};
            t.setTerm(parseTemplate("{a}{b}", t));
            check.typeTemplate(env, t);
        });
    }


    @Test
    public <X> void keyTypeLookupWithKnown() {
        add(new L<String>(){}, "A");

        T<X> t = new T<>(){};
        t.setTerm(parseTemplate("{A}", t));

        check.typeTemplate(env, t);
    }

    @Test
    public <X> void keyTypeLookupComposition() {
        add(new L<X>(){}, "A");

        {
            T<X> known = new T<>(){};
            known.setTerm(parseTemplate("my str", known));
            check.typeTemplate(env, known);
        }

        T<X> t = new T<>(){};
        t.setTerm(parseTemplate("{A}", t));

        check.typeTemplate(env, t);
    }

    @Test
    public <X> void keyLookupWithUnknown() {
        add(new L<X>(){}, "A");

        T<String> t = new T<>(){};
        t.setTerm(parseTemplate("{A}", t));

        check.typeTemplate(env, t);
    }

    @Test
    public <X> void listCapture() throws ValidationException {
        add(new L<String>(){}, "A");

        T<X> t = new T<>(){};
        t.setTerm(parseTemplate(List.of("{A}"), t));

        check.typeTemplate(env, t);
        assertEquals(
                "java.util.List<java.lang.String>",
                check.captures.get("X").get().getTypeName()
        );
    }

    @Test
    public <X> void replaceTypeVariableWithStaticType() {
        T<X> t = new T<>(){};
        t.setTerm(parseTemplate("static", t));

        check.typeTemplate(env, t);
        assertEquals(
                "java.lang.String",
                check.captures.get("X").getTypeString()
        );
    }

    @Test
    public <X> void listEmptyCapture() throws ValidationException {
        T<X> t = new T<>(){};
        t.setTerm(parseTemplate(List.of(), t));

        check.typeTemplate(env, t);
        assertEquals(
                "java.util.List<java.lang.Object>",
                check.captures.get("X").get().getTypeName()
        );
    }

    @Test
    public <X> void listEmptyCaptureComposition() throws ValidationException {
        add(new L<X>(){}, "A");

        T<X> t = new T<>(){};
        t.setTerm(parseTemplate(List.of(), t));
        check.typeTemplate(env, t);

        T<List<String>> t2 = new T<>(){};
        t2.setTerm(parseTemplate("{A}", t2));
        check.typeTemplate(env, t2);

        assertEquals(
                "java.util.List<java.lang.String>",
                check.resolve("X").getTypeString()
        );
    }

    @Test
    public <X, Y> void listEmptyCaptureComposition2() throws ValidationException {
        add(new L<X>(){}, "A");

        T<X> t = new T<>(){};
        t.setTerm(parseTemplate(List.of(), t));

        check.typeTemplate(env, t);

        T<Y> t2 = new T<>(){};
        t2.setTerm(parseTemplate(List.of("{A}", List.of("cool", "list", "ofstring")), t2));
        check.typeTemplate(env, t2);
        assertEquals(
                "java.util.List<java.util.List<java.lang.String>>",
                check.resolve("Y").getTypeString()
        );
    }

    @Test
    public <X, Y> void listEmptyCaptureComposition3() throws ValidationException {
        add(new L<X>(){}, "A");

        T<X> t = new T<>(){};
        t.setTerm(parseTemplate(List.of(), t));
        check.typeTemplate(env, t);


        T<Y> t2 = new T<>(){};
        t2.setTerm(parseTemplate(List.of("{A}", List.of("cool", "ofstring"), List.of()), t2));
        check.typeTemplate(env, t2);
        assertEquals(
                "java.util.List<java.util.List<java.lang.String>>",
                check.resolve("Y").get().getTypeName()
        );
    }

    @Test
    public <X> void listEmptyCaptureCompositionFail() throws ValidationException {
        assertThrows(TemplateException.class,() -> {
            T<X> t = new T<>(){};
            t.setTerm(parseTemplate(List.of(
                    List.of(List.of("ok"), List.of("tes")),
                    List.of(List.of("String"), List.of()),
                    12
            ), t));

            check.typeTemplate(env, t);

            assertEquals(
                    "java.util.List<java.util.List<java.util.List<java.lang.String>>>",
                    check.captures.get("X").get().getTypeName()
            );
        });
    }


    @Test
    public <X> void listEmptyCaptureCompositionFail2() throws ValidationException {
        assertThrows(TemplateException.class,() -> {
            T<X> t = new T<>(){};
            t.setTerm(parseTemplate(List.of(
                    List.of(List.of()),  // X$ListOf :: List<List<Object>>
                    List.of(), // X$ListOf :: List<Object>
                    12
            ), t));

            check.typeTemplate(env, t);
        });
    }

    @Test
    public <X> void mapEmptyCaptureCompositionFail2() throws ValidationException {
        assertThrows(TemplateException.class,() -> {
            T<X> t = new T<>(){};
            t.setTerm(parseTemplate(Map.of(
                    "1", Map.of("1", Map.of()),  // X$MapOf :: Map<Map<Object>>
                    "2", Map.of(), // X$MapOf :: Map<Object>
                    "3", 12
            ), t));

            check.typeTemplate(env, t);
        });
    }

    @Test
    public <X> void mapEmptyCaptureCompositionOk() throws ValidationException {
        T<X> t = new T<>(){};
        t.setTerm(parseTemplate(Map.of(
                "1", Map.of("1", Map.of()),  // X$MapOf :: Map<Map<Object>>
                "2", Map.of() // X$MapOf :: Map<Object>
        ), t));

        check.typeTemplate(env, t);
        assertEquals(
                "java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.util.Map<class java.lang.String, class java.lang.Object>>>",
                check.captures.get("X").get().getTypeName()
        );
    }

    @Test
    public <X> void mapEmptyCapture() throws ValidationException {
        T<X> t = new T<>(){};
        t.setTerm(parseTemplate(Map.of(), t));

        check.typeTemplate(env, t);
        assertEquals(
                "java.util.Map<java.lang.String, java.lang.Object>",
                check.captures.get("X").get().getTypeName()
        );
    }

    @Test
    public <X> void mapEmptyCaptureComposition() throws ValidationException {
        add(new L<X>(){}, "A");

        T<X> t = new T<>(){};
        t.setTerm(parseTemplate(Map.of(), t));
        check.typeTemplate(env, t);

        T<Map<String, String>> t2 = new T<>(){};
        t2.setTerm(parseTemplate("{A}", t2));
        check.typeTemplate(env, t2);
        assertEquals(
                "java.util.Map<java.lang.String, java.lang.String>",
                check.captures.get("X").get().getTypeName()
        );
    }

    @Test
    public <X, Y> void mapEmptyCaptureComposition2() throws ValidationException {
        T<X> t = new T<>(){};
        t.setTerm(parseTemplate(Map.of(), t));

        check.typeTemplate(env, t);
        assertEquals(
                "java.util.Map<java.lang.String, java.lang.Object>",
                check.resolve("X").get().getTypeName()
        );

        add(new L<X>(){}, "A");

        T<Y> t2 = new T<>(){};
        t2.setTerm(parseTemplate(
                Map.of("hello", "{A}", "test", Map.of("ok", "cool")),
                t2)
        );

        check.typeTemplate(env, t2);
        assertEquals(
                "java.util.Map<java.lang.String, java.util.Map<class java.lang.String, class java.lang.String>>",
                check.resolve("Y").getTypeString()
        );
    }


    @Test
    public <X> void nestedBindingOfTypeVariables() throws ValidationException {
        T<X> t = new T<>(){};
        t.setTerm(parseTemplate(List.of(), t));
        check.typeTemplate(env, t);

        assertTrue(check.captures.containsKey("X"));
        assertTrue(check.captures.containsKey("X$ListOf"));

        assertEquals( "java.util.List<java.lang.Object>", check.captures.get("X").getTypeString() );
        assertEquals( "java.lang.Object", check.captures.get("X$ListOf").getTypeString() );
    }

    @Test
    public <X> void nestedBindingOfTypeVariables2() throws ValidationException {
        T<X> t = new T<>(){};
        t.setTerm(parseTemplate(List.of(List.of()), t));
        check.typeTemplate(env, t);

        assertTrue(check.captures.containsKey("X"));
        assertTrue(check.captures.containsKey("X$ListOf"));
        assertTrue(check.captures.containsKey("X$ListOfListOf"));

        assertEquals( "java.util.List<java.util.List<class java.lang.Object>>", check.captures.get("X").getTypeString() );
        assertEquals( "java.util.List<java.lang.Object>", check.captures.get("X$ListOf").getTypeString() );
        assertEquals( "java.lang.Object", check.captures.get("X$ListOfListOf").getTypeString() );
    }

    @Test
    public <X> void nestedBindingOfTypeVariables3() throws ValidationException {
        T<X> t = new T<>(){};
        t.setTerm(parseTemplate(List.of(List.of("String"), List.of()), t));
        check.typeTemplate(env, t);

        assertTrue(check.captures.containsKey("X"));
        assertTrue(check.captures.containsKey("X$ListOf"));
        assertTrue(check.captures.containsKey("X$ListOfListOf"));

        assertEquals( "java.util.List<java.util.List<java.lang.String>>", check.resolve("X").getTypeString() );
        assertEquals( "java.util.List<java.lang.String>", check.captures.get("X$ListOf").getTypeString() );
        assertEquals( "java.lang.String", check.captures.get("X$ListOfListOf").getTypeString() );
    }




    // **********************
    // CONVERT TO INT FEATURE DISABLED
    // **********************

    @Disabled // TODO check if we can treat Integer as String when it makes sense
    @Nested
    @DisplayName("Treat Int as String")
    class IntAsString {
        @Test
        public void nestedKeyLookupConvertToInt() {
            T<String> t = new T<>(){};
            t.setTerm(parseTemplate("{{A}}", t));

            { // A :: String
                T<String> known = new T<>(){};
                known.setTerm(parseTemplate("A", known));
                check.add(env, known.getTerm(), known);
            }
            { // {A} :: Integer
                T<Integer> known = new T<>(){};
                known.setTerm(parseTemplate("{A}", known));
                check.add(env, known.getTerm(), known);
            }

            check.typeTemplate(env, t);
        }

        @Test
        public void simpleKeyLookupInListConvertType() throws ValidationException {
            T<List<String>> t = new T<>(){};
            t.setTerm(parseTemplate(List.of("{A}"), t));

            { // A :: Integer
                T<Integer> known = new T<>(){};
                known.setTerm(parseTemplate("A", known));
                check.add(env, known.getTerm(), known);
            }

            check.typeTemplate(env, t);
        }

        @Test
        public <A> void simpleConstantListWithCaptureTwiceIntToString() throws ValidationException {
            {
                T<A> t = new T<>(){};
                t.setTerm(parseTemplate(List.of("hello", "test"), t));
                check.typeTemplate(env, t);
            }

            {
                T<A> t = new T<>(){};
                t.setTerm(parseTemplate(List.of(42, 42), t));
                check.typeTemplate(env, t);
            }
        }

        @Test
        public <A> void simpleConstantListWithCaptureTwiceIntToString2() throws ValidationException {
            {
                T<A> t = new T<>(){};
                t.setTerm(parseTemplate(List.of("hello", "test"), t));
                check.typeTemplate(env, t);
            }

            {
                T<A> t = new T<>(){};
                t.setTerm(parseTemplate(List.of(42, "Still string"), t));
                check.typeTemplate(env, t);
            }
        }

        @Test
        public <A> void simpleConstantListWithIntToStringCapture() throws ValidationException {
            T<A> t = new T<>(){};
            t.setTerm(parseTemplate(List.of("hello", "test", 1), t));
            check.typeTemplate(env, t);
        }

        @Test
        public void simpleMapLookupConvertToIntOk() {
            T<String> t = new T<>(){};
            t.setTerm(parseTemplate("{{map}@key}", t));

            { // map :: Map<String, Integer>
                T<Map<String, Integer>> known = new T<>(){};
                known.setTerm(parseTemplate("map", known));
                check.add(env, known.getTerm(), known);
            }

            check.typeTemplate(env, t);
        }

        @Test
        public <A> void simpleConstantStringWithCaptureTwiceFail() throws ValidationException {
            T<A> t = new T<>(){};
            t.setTerm(parseTemplate("hello", t));
            check.typeTemplate(env, t);

            T<A> t2 = new T<>(){};
            t2.setTerm(parseTemplate(42, t2));
            check.typeTemplate(env, t2);
        }

        @Test
        public void nestedKeyLookupConvertToInt2() {
            { // A :: Integer
                T<Integer> known = new T<>(){};
                known.setTerm(parseTemplate("A", known));
                check.typeTemplate(env, known);
                check.add(env, known.getTerm(), known);
            }
            { // {A} :: String
                T<String> known = new T<>(){};
                known.setTerm(parseTemplate("{A}", known));
                check.typeTemplate(env, known);
                check.add(env, known.getTerm(), known);
            }

            {
                T<String> t = new T<>(){};
                t.setTerm(parseTemplate("{{A}}", t));
                check.typeTemplate(env, t);
            }
        }

        @Test
        public void simpleListLookupConvertIntToStringOk() {
            add(new L<List<Integer>>(){}, "list");

            T<String> t = new T<>(){};
            t.setTerm(parseTemplate("{{list}}[0]", t));

            check.typeTemplate(env, t);
        }

        @Test
        public <A> void badListType() throws ValidationException {
            add(new L<List<String>>(){}, "list");

            { // A :: List<String>
                T<A> t = new T<>(){};
                t.setTerm(parseTemplate("{list}", t));
                check.typeTemplate(env, t);
            }

            { // A cant be Integer anymore
                T<A> t = new T<>(){};
                t.setTerm(parseTemplate(1, t));
                check.typeTemplate(env, t);
            }
        }

    }

    @Test
    public <A,B> void specializeType() throws ValidationException {
        add(new L<Object>(){}, "obj");

        { // obj :: Object ~> List<A>
            T<A> t = new T<>(){};
            t.setTerm(parseTemplate("{{obj}}[0]", t));
            check.typeTemplate(env, t);
        }

        { // obj :: List<B>
            T<List<B>> t = new T<>(){};
            t.setTerm(parseTemplate("{obj}", t));
            check.typeTemplate(env, t);
        }
    }

    @Test
    public <A,B> void specializeTypeFail() throws ValidationException {
        assertThrows(TemplateException.class,() -> {
            add(new L<Object>(){}, "obj");

            { // obj :: Object ~> List<A>
                T<A> t = new T<>(){};
                t.setTerm(parseTemplate("{{obj}}[0]", t));
                check.typeTemplate(env, t);
            }

            { // obj :: String !
                T<String> t = new T<>(){};
                t.setTerm(parseTemplate("{obj}", t));
                check.typeTemplate(env, t);
            }
        });
    }

    private void add(L<?> loc, String key) {
        {
            loc.setLocation( new TemplateConstant<>(key, new T<String>(){}) );
            check.add(env, loc.getLocation(), loc.getTarget());
        }
    }

    @Test
    public void isThisPossibleOrWhat() throws ValidationException {
        {
            T<Object> t = new T<>(){};
            t.setTerm(parseTemplate(
                    Map.of(
                            "mystring", "xyz"
                            , "inttoo", 2
                            , "listisalsoOK", List.of("really?")
                    )
                    , t));
            check.typeTemplate(env, t);
        }
    }

    @Test
    public void isThisPossibleOrWhat3() throws ValidationException {
        add(new L<List<String>>(){}, "ok");

        { // obj :: Object
            T<Object> t = new T<>(){};
            t.setTerm(parseTemplate("{ok}" , t));
            check.typeTemplate(env, t);
        }
    }

    @Test
    public void isThisPossibleOrWhat4() throws ValidationException {
        add(new L<List<String>>(){}, "ok");

        { // obj :: Object
            T<Object> t = new T<>(){};
            t.setTerm(parseTemplate("{{ok}}[0]" , t));
            check.typeTemplate(env, t);
        }
    }

    @Test
    public void isThisPossibleOrWhat5() throws ValidationException {
        assertThrows(TemplateException.class,() -> {
            add(new L<List<String>>(){}, "ok");

            { // obj :: Object
                T<Object> t = new T<>(){};
                t.setTerm(parseTemplate("{{{{ok}}[0]@toomuch}" , t));
                check.typeTemplate(env, t);
            }
        });
    }

    @Test
    public void isThisPossibleOrWhat6() throws ValidationException {
        assertThrows(TemplateException.class,() -> {
            add(new L<List<String>>(){}, "ok");

            { // obj :: Object
                T<Object> t = new T<>(){};
                t.setTerm(parseTemplate("{{ok}@notmap}" , t));
                check.typeTemplate(env, t);
            }
        });
    }
}
