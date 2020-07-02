package scraper.api.template;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

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
        Assert.assertEquals(String.class, new TypeGeneralizer(Map.of()){}.visit(String.class, String.class));
    }

    @Test
    public void symmetricReplace()  {
        Assert.assertEquals(String.class, new TypeGeneralizer(Map.of()){}.visit(Object.class, String.class));
        Assert.assertEquals(String.class, new TypeGeneralizer(Map.of()){}.visit(String.class, Object.class));
    }

    @Test
    public void wrongType()  {
        Assert.assertNull(new TypeGeneralizer(Map.of()){}.visit(Integer.class, String.class));
    }

    @Test
    public <X> void sameTypeCapture()  {
        Assert
                .assertEquals(String.class, new TypeGeneralizer(Map.of("X", TS)){}
                        .visit(String.class, new T<X>(){}.get()));

        Assert
                .assertEquals(String.class, new TypeGeneralizer(Map.of("X", TS)){}
                        .visit(new T<X>(){}.get(), String.class));
    }

    @Test
    public void parameterizedTypeObj()  {
        Assert.assertEquals(
                TL.get(), new TypeGeneralizer(Map.of()){}.visit(
                        TL.get(),
                        Object.class
                ));

        Assert.assertEquals(
                TL.get(), new TypeGeneralizer(Map.of()){}.visit(
                        Object.class,
                        TL.get()
                ));
    }

    @Test
    public void parameterizedTypeOk()  {
        Assert.assertEquals(
                TL.get(), new TypeGeneralizer(Map.of()){}.visit(
                        TL.get(),
                        TL.get()
        ));
    }

    @Test
    public void parameterizedTypeBad()  {
        Assert.assertNull(
                 new TypeGeneralizer(Map.of()){}.visit(
                        TL.get(),
                        TLI.get()
                ));
    }

    @Test
    public <X> void sameTypeCaptureTypeVarR() {
        Assert.assertEquals(TL.get(),
                new TypeGeneralizer(Map.of("X", TL)) {}
                        .visit(
                                TL.get(),
                                new T<X>() {}.get()
                        ));
    }

    @Test
    public <X> void sameTypeCaptureTypeVar() {
        Assert.assertEquals(TL.get(),
                new TypeGeneralizer(Map.of("X", TL)) {}
                        .visit(
                                new T<X>() {}.get(),
                                TL.get()
                        ));
    }

    @Test
    public <X, Y> void twoTypeVars() {
        Assert.assertEquals(TL.get(),
                new TypeGeneralizer(Map.of("Y", TL)) {}
                        .visit(
                                new T<X>() {}.get(),
                                new T<Y>() {}.get()
                        ));
    }

    @Test
    public <X> void sameVar() {
        Assert.assertEquals(TS.get(),
                new TypeGeneralizer(Map.of("X", TS)) {}
                        .visit(
                                new T<X>() {}.get(),
                                new T<X>() {}.get()
                        ));
    }

    @Test
    public void parList() {
        Assert.assertEquals(TL.get(),
                new TypeGeneralizer(Map.of()) {}
                        .visit(
                                TL.get(),
                                new T<List<Object>>() {}.get()
                        ));
    }

    @Test
    public void parListR() {
        Assert.assertEquals(TL.get(),
                new TypeGeneralizer(Map.of()) {}
                        .visit(
                                new T<List<Object>>() {}.get(),
                                TL.get()
                        ));
    }

    @Test
    public void toStr() {
        Assert.assertEquals("java.util.Map<java.lang.String, java.lang.Integer>",
                new TypeGeneralizer(Map.of()) {
        }.visit(TLM.get(), TLM.get()).toString());
    }

    @Test
    public <A> void mapTest2() {
        Assert.assertNotNull(new TypeGeneralizer(Map.of("A", TS)) {}
                .visit(
                        new T<Map<String, String>>() {}.get(),
                        new T<Map<String, A>>() {}.get()
                ));
    }
    @Test
    public <A> void mapTest() {
        Assert.assertNull(new TypeGeneralizer(Map.of("A", TLMS)) {}
                .visit(
                        new T<Map<String, String>>() {}.get(),
                        new T<Map<String, A>>() {}.get()
                ));
    }

    @Test
    public <A> void listTest() {
        TypeGeneralizer lizer = new TypeGeneralizer(Map.of()) {};
        Assert.assertEquals(
                new T<List<Map<String, String>>>(){}.get(), lizer
                .visit(
                        new T<List<Map<String, String>>>() {}.get(),
                        new T<List<A>>() {}.get()
                ));

        Assert.assertTrue(lizer.newCaptures.containsKey("A"));
    }
}