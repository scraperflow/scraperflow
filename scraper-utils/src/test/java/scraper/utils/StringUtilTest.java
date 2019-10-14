package scraper.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Set;

public class StringUtilTest {
    @Test
    public void removeExtensionTest() {
        String abc = "a" + File.separator + "b" + File.separator + "c";
        Assert.assertEquals("hello", StringUtil.removeExtension("hello.txt"));
        Assert.assertEquals("hello", StringUtil.removeExtension("hello."));
        Assert.assertEquals("hello", StringUtil.removeExtension("hello"));
        Assert.assertEquals("hello", StringUtil.removeExtension("hello"));
        Assert.assertEquals(abc, StringUtil.removeExtension(abc+".jpg"));
        Assert.assertEquals(abc, StringUtil.removeExtension(abc));
        Assert.assertEquals("a.b"+File.separator+"c", StringUtil.removeExtension("a.b"+File.separator+"c"));
        Assert.assertEquals("a.b"+File.separator+"c", StringUtil.removeExtension("a.b"+File.separator+"c.txt"));
    }

    @Test
    public void pathProductTest() {
        Assert.assertEquals(Set.of("a","b","a/c","b/c"), StringUtil.pathProduct(Set.of("a", "b"), Set.of("c")));
    }

    @Test
    public void getArgumentTest() {
        String t = StringUtil.getArgument(new String[]{"param:abc"}, "param");
        Assert.assertEquals("abc", t);
    }

    @Test
    public void getArgumentNoParamTest() {
        String t = StringUtil.getArgument(new String[]{"help"}, "help");
        Assert.assertEquals("", t);
    }

}
