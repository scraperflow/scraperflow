package scraper.api.reflect;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the type token T
 *
 * @since 1.0.0
 */
public class TypeTokenTest {

    @Test
    public void simpleTest()  {
        T<Integer> simpleType = new T<>(){};
        T<Integer> simpleType2 = new T<>(){};
        T<String> simpleStringType = new T<>(){};

        Assert.assertEquals(simpleType, simpleType2);
        Assert.assertNotEquals(simpleType, simpleStringType);

        Assert.assertEquals(simpleType.get(), simpleType2.get());
        Assert.assertNotEquals(simpleType.get(), simpleStringType.get());

        Assert.assertEquals(simpleType.hashCode(), simpleType2.hashCode());
        Assert.assertNotEquals(simpleType, new Object());
    }

    @Test
    public void jsonEqualsSimpleTest()  {
        T<Integer> int1 = new T<>(){};
        int1.setParsedJson(5);

        T<Integer> int2 = new T<>(){};
        int2.setParsedJson(5);

        T<Integer> int3 = new T<>(){};
        int3.setParsedJson(-1);

        Assert.assertEquals(int1, int2);
        Assert.assertNotEquals(int1, int3);
    }

    @Test
    public void jsonEqualsGrammarTest()  {
        T<Integer> int1 = new T<>(){};
        int1.setParsedJson("{my-int}");

        T<Integer> int2 = new T<>(){};
        int2.setParsedJson(5);

        T<Integer> int3 = new T<>(){};
        int3.setParsedJson("{my-int}");

        Assert.assertEquals(int1, int3);
        Assert.assertNotEquals(int1, int2);
    }

    @Test
    public void rawJsonTest()  {
        T<Integer> int1 = new T<>(){};
        int1.setParsedJson("{my-int}");
        Assert.assertEquals(int1.getRawJson(), "{my-int}");
        Assert.assertEquals(int1.getParsedJson(), "{my-int}");
        Assert.assertEquals(int1.toString(), "{my-int}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void doNotExtendTest()  { new TTest<Integer>(){}; }
    private static class TTest<TYPE> extends T<TYPE> {}

    @Test(expected = IllegalArgumentException.class)
    public void doParameterizeTypeTest()  { new T(){}; }
}