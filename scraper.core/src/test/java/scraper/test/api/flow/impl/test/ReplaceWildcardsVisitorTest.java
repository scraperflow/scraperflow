package scraper.test.api.flow.impl.test;

import org.junit.jupiter.api.Test;
import scraper.api.T;
import scraper.api.flow.impl.ReplaceWildcardsVisitor;

import java.lang.reflect.Type;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReplaceWildcardsVisitorTest {

    @Test
    public void simpleTypesMatchTest() {
        var known = new T<String>(){}.get();
        Type newType = new ReplaceWildcardsVisitor() {}.visit(known);
        assertEquals(String.class, known);
        assertEquals(known, newType);
        assertEquals(newType, newType);
    }

    @Test
    public void simpleMapTest() {
        var known = new T<Map<String, Integer>>(){}.get();
        Type newType = new ReplaceWildcardsVisitor() {}.visit(known);
        assertEquals(known, newType);
        assertEquals(newType, newType);
    }

    @Test
    public void keepWildcardTest() {
        var known = new T<Map<String, ?>>(){}.get();
        Type newType = new ReplaceWildcardsVisitor() {}.visit(known);
        assertEquals(known, newType);
        assertEquals(newType, newType);
    }

    @Test
    public <K> void makeWildcardTest() {
        var known = new T<Map<String, K>>(){}.get();
        Type newType = new ReplaceWildcardsVisitor() {}.visit(known);
        assertEquals(new T<Map<String, ?>>(){}.get(), newType);
        assertEquals(newType, newType);
    }

    @Test
    public <K> void makeWildcardWithWildcardTest() {
        var known = new T<Map<?, K>>(){}.get();
        Type newType = new ReplaceWildcardsVisitor() {}.visit(known);
        assertEquals(new T<Map<?, ?>>(){}.get(), newType);
    }

    @Test
    public void reflexivity() {
        var known = new T<Map<?, ?>>(){}.get();
        Type newType = new ReplaceWildcardsVisitor() {}.visit(known);
        Type newType2 = new ReplaceWildcardsVisitor() {}.visit(known);
        assertEquals(newType, newType2);
    }
}