package scraper.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class StringUtilTest {
    @Test
    public void removeExtensionTest() {
        Assert.assertEquals("hello", StringUtil.removeExtension("hello.txt"));
        Assert.assertEquals("hello", StringUtil.removeExtension("hello."));
        Assert.assertEquals("hello", StringUtil.removeExtension("hello"));
    }

    @Test
    public void pathProductTest() {
        Assert.assertEquals(Set.of("a","b","a/c","b/c"), StringUtil.pathProduct(Set.of("a", "b"), Set.of("c")));
    }

}
