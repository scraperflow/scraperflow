package scraper.api.flow.impl;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.template.T;

import java.lang.reflect.Type;
import java.util.Map;

public class ReplaceWildcardsVisitorTest {

    @Test
    public void simpleTypesMatchTest() {
        var known = new T<String>(){}.get();
        Type newType = new ReplaceWildcardsVisitor() {}.visit(known);
        Assert.assertEquals(String.class, known);
        Assert.assertEquals(known, newType);
        Assert.assertEquals(newType, newType);
    }

    @Test
    public void simpleMapTest() {
        var known = new T<Map<String, Integer>>(){}.get();
        Type newType = new ReplaceWildcardsVisitor() {}.visit(known);
        Assert.assertEquals(known, newType);
        Assert.assertEquals(newType, newType);
    }

    @Test
    public void keepWildcardTest() {
        var known = new T<Map<String, ?>>(){}.get();
        Type newType = new ReplaceWildcardsVisitor() {}.visit(known);
        Assert.assertEquals(known, newType);
        Assert.assertEquals(newType, newType);
    }

    @Test
    public <K> void makeWildcardTest() {
        var known = new T<Map<String, K>>(){}.get();
        Type newType = new ReplaceWildcardsVisitor() {}.visit(known);
        Assert.assertEquals(new T<Map<String, ?>>(){}.get(), newType);
        Assert.assertEquals(newType, newType);
    }

    @Test
    public <K> void makeWildcardWithWildcardTest() {
        var known = new T<Map<?, K>>(){}.get();
        Type newType = new ReplaceWildcardsVisitor() {}.visit(known);
        Assert.assertEquals(new T<Map<?, ?>>(){}.get(), newType);
    }

    @Test
    public void reflexivity() {
        var known = new T<Map<?, ?>>(){}.get();
        Type newType = new ReplaceWildcardsVisitor() {}.visit(known);
        Type newType2 = new ReplaceWildcardsVisitor() {}.visit(known);
        Assert.assertEquals(newType, newType2);
    }
}