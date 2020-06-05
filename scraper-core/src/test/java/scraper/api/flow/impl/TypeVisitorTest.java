package scraper.api.flow.impl;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.exceptions.TemplateException;
import scraper.api.template.T;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TypeVisitorTest {

    @Test
    public void simpleTypesMatchTest() {
        var known = new T<String>(){}.get();
        var target = new T<String>(){}.get();
        new GenericTypeMatcher(){}.visit(known, target);
    }

    @Test(expected = TemplateException.class)
    public void simpleTypesFailTest() {
        var known = new T<String>(){}.get();
        var target = new T<Integer>(){}.get();
        new GenericTypeMatcher(){}.visit(known, target);
    }

    @Test
    public <K> void genericTargetTest() {
        var known = new T<String>(){}.get();
        var target = new T<K>(){}.get();
        new GenericTypeMatcher(){}.visit(known, target);
    }

    @Test(expected = TemplateException.class)
    public <K> void failTest() {
        var known = new T<String>(){}.get();
        var target = new T<Map<K, String>>(){}.get();
        new GenericTypeMatcher(){}.visit(known, target);
    }

    @Test
    public <K> void doubleCaptureTest() {
        var known = new T<Map<String, String>>(){}.get();
        var target = new T<Map<K, K>>(){}.get();
        new GenericTypeMatcher(){}.visit(known, target);
    }

    @Test(expected = TemplateException.class)
    public <K> void doubleCaptureFailTest() {
        var known = new T<Map<Integer, String>>(){}.get();
        var target = new T<Map<K, K>>(){}.get();
        new GenericTypeMatcher(){}.visit(known, target);
    }

    @Test
    public <K,V,X> void multipleCapturesTest() {
        var known = new T<Map<Integer, List<Map<String, Integer>>>>(){}.get();
        var target = new T<Map<V, List<Map<X, K>>>>(){}.get();
        new GenericTypeMatcher(){}.visit(known, target);
    }

    @Test
    public void targetIsMoreSpecializedTest() {
        var known = new T<List<?>>(){}.get();
        var target = new T<List<String>>(){}.get();
        Assert.assertTrue(new GenericTypeMatcher(){}.visit(known, target));
    }

    @Test
    public void targetIsUnknownTarget() {
        var known = new T<List<?>>(){}.get();
        var target = new T<List<?>>(){}.get();
        Assert.assertFalse(new GenericTypeMatcher(){}.visit(known, target));
    }

    @Test
    public <V> void targetIsMoreSpecializedGenericTest() {
        var known = new T<List<?>>(){}.get();
        var target = new T<List<V>>(){}.get();
        new GenericTypeMatcher(){}.visit(known, target);
    }

    @Test
    public void targetIsMoreSpecializedActualTest() {
        var known = new T<List<?>>(){}.get();
        var target = new T<List<String>>(){}.get();
        Assert.assertTrue(new GenericTypeMatcher(){}.visit(known, target));
    }

    @Test
    public void targetIsMoreSpecializedNestedTest() {
        // this should never happen but should not throw exception, just log an error
        var known = new T<List<List<String>>>(){}.get();
        var target = new T<List<List<?>>>(){}.get();
        new GenericTypeMatcher(){}.visit(known, target);
    }

    @Test
    public <V> void problemTest() {
        // this should never happen but should not throw exception, just log an error
        var known = new T<List<? extends String>>(){}.get();
        var target = new T<List<V>>(){}.get();
        new GenericTypeMatcher(){}.visit(known, target);
    }

    @Test
    public <V> void moreSpecializedButStillUnknownsTest() {
        var known = new T<Map<?, ?>>(){}.get();
        var target = new T<Map<String, V>>(){}.get();
        Assert.assertTrue(new GenericTypeMatcher(){}.visit(known, target));
    }

    @Test(expected = SpecializeException.class)
    public void knownIsSubtypeFailTest() {
        var known = new T<>(){}.get();
        var target = new T<Map<String, Integer>>(){}.get();
        Assert.assertFalse(new GenericTypeMatcher(){}.visit(known, target));
    }

    @Test(expected = TemplateException.class)
    public void badTypeTest() {
        var known = new T<Function<String, Integer>>(){}.get();
        var target = new T<Map<String, Integer>>(){}.get();
        new GenericTypeMatcher(){}.visit(known, target);
    }


    @Test
    public void mapFunctionsTest() {
        var known = new T<Map<String, String>>(){}.get();
        var target = new T<Map<String, String>>(){}.get();

        new GenericTypeMatcher(){}.visit(known, target);
    }
}