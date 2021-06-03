package scraper.api.template;

import org.junit.jupiter.api.Test;
import scraper.api.T;
import scraper.api.TypeGeneralizer;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the type generalizing
 */
public class TypeGeneralizerTest {

    T<String> TS = new T<>(){};
    T<List<String>> TL = new T<>(){};
    T<List<Integer>> TLI = new T<>(){};
    T<Map<String, Integer>> TLM = new T<>(){};
    T<Map<String, String>> TLMS = new T<>(){};

    @Test
    public void sameType()  {
        assertEquals(String.class, new TypeGeneralizer(Map.of()){}.visit(String.class, String.class));
    }

    @Test
    public void symmetricReplace()  {
        assertEquals(String.class, new TypeGeneralizer(Map.of()){}.visit(Object.class, String.class));
        assertEquals(String.class, new TypeGeneralizer(Map.of()){}.visit(String.class, Object.class));
    }

    @Test
    public void wrongType()  {
        assertNull(new TypeGeneralizer(Map.of()){}.visit(Integer.class, String.class));
    }

    @Test
    public <X> void sameTypeCapture()  {
      assertEquals(String.class, new TypeGeneralizer(Map.of("X", TS)){}
                        .visit(String.class, new T<X>(){}.get()));

      assertEquals(String.class, new TypeGeneralizer(Map.of("X", TS)){}
                        .visit(new T<X>(){}.get(), String.class));
    }

    @Test
    public void parameterizedTypeObj()  {
        assertEquals(
                TL.get(), new TypeGeneralizer(Map.of()){}.visit(
                        TL.get(),
                        Object.class
                ));

        assertEquals(
                TL.get(), new TypeGeneralizer(Map.of()){}.visit(
                        Object.class,
                        TL.get()
                ));
    }

    @Test
    public void parameterizedTypeOk()  {
        assertEquals(
                TL.get(), new TypeGeneralizer(Map.of()){}.visit(
                        TL.get(),
                        TL.get()
        ));
    }

    @Test
    public void parameterizedTypeBad()  {
        assertNull(
                 new TypeGeneralizer(Map.of()){}.visit(
                        TL.get(),
                        TLI.get()
                ));
    }

    @Test
    public <X> void sameTypeCaptureTypeVarR() {
        assertEquals(TL.get(),
                new TypeGeneralizer(Map.of("X", TL)) {}
                        .visit(
                                TL.get(),
                                new T<X>() {}.get()
                        ));
    }

    @Test
    public <X> void sameTypeCaptureTypeVar() {
        assertEquals(TL.get(),
                new TypeGeneralizer(Map.of("X", TL)) {}
                        .visit(
                                new T<X>() {}.get(),
                                TL.get()
                        ));
    }

    @Test
    public <X, Y> void twoTypeVars() {
        assertEquals(TL.get(),
                new TypeGeneralizer(Map.of("Y", TL)) {}
                        .visit(
                                new T<X>() {}.get(),
                                new T<Y>() {}.get()
                        ));
    }

    @Test
    public <X> void sameVar() {
        assertEquals(TS.get(),
                new TypeGeneralizer(Map.of("X", TS)) {}
                        .visit(
                                new T<X>() {}.get(),
                                new T<X>() {}.get()
                        ));
    }

    @Test
    public void parList() {
        assertEquals(TL.get(),
                new TypeGeneralizer(Map.of()) {}
                        .visit(
                                TL.get(),
                                new T<List<Object>>() {}.get()
                        ));
    }

    @Test
    public void parListR() {
        assertEquals(TL.get(),
                new TypeGeneralizer(Map.of()) {}
                        .visit(
                                new T<List<Object>>() {}.get(),
                                TL.get()
                        ));
    }

    @Test
    public void toStr() {
        assertEquals("java.util.Map<java.lang.String, java.lang.Integer>",
                new TypeGeneralizer(Map.of()) {
        }.visit(TLM.get(), TLM.get()).toString());
    }

    @Test
    public <A> void mapTest2() {
        assertNotNull(new TypeGeneralizer(Map.of("A", TS)) {}
                .visit(
                        new T<Map<String, String>>() {}.get(),
                        new T<Map<String, A>>() {}.get()
                ));
    }
    @Test
    public <A> void mapTest() {
        assertNull(new TypeGeneralizer(Map.of("A", TLMS)) {}
                .visit(
                        new T<Map<String, String>>() {}.get(),
                        new T<Map<String, A>>() {}.get()
                ));
    }

    @Test
    public <A> void listTest() {
        TypeGeneralizer lizer = new TypeGeneralizer(Map.of()) {};
        assertEquals(
                new T<List<Map<String, String>>>(){}.get(), lizer
                .visit(
                        new T<List<Map<String, String>>>() {}.get(),
                        new T<List<A>>() {}.get()
                ));

        assertTrue(lizer.newCaptures.containsKey("A"));
    }
}