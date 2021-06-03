package scraper.api.template;

import org.junit.jupiter.api.Test;
import scraper.api.T;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the type token T
 */
public class TypeTokenTest {

    @Test
    public void simpleTest()  {
        T<Integer> simpleType = new T<>(){};
        T<Integer> simpleType2 = new T<>(){};
        T<String> simpleStringType = new T<>(){};

        assertEquals(simpleType, simpleType2);
        assertNotEquals(simpleType, simpleStringType);

        assertEquals(simpleType.get(), simpleType2.get());
        assertNotEquals(simpleType.get(), simpleStringType.get());

        assertEquals(simpleType.hashCode(), simpleType2.hashCode());
        assertNotEquals(simpleType, new Object());
    }

    @Test
    public void doNotExtendTest()  { assertThrows(IllegalArgumentException.class, () -> new TTest<Integer>(){}); }
    private static class TTest<TYPE> extends T<TYPE> {}

    @SuppressWarnings("rawtypes") // under test
    @Test
    public void doParameterizeTypeTest()  { assertThrows(IllegalArgumentException.class, () -> new T(){}); }
}