package scraper.utils;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StringUtilTest {
    @Test
    public void removeExtensionTest() {
        String abc = "a" + File.separator + "b" + File.separator + "c";
        assertEquals("hello", StringUtil.removeExtension("hello.txt"));
        assertEquals("hello", StringUtil.removeExtension("hello."));
        assertEquals("hello", StringUtil.removeExtension("hello"));
        assertEquals("hello", StringUtil.removeExtension("hello"));
        assertEquals(abc, StringUtil.removeExtension(abc+".jpg"));
        assertEquals(abc, StringUtil.removeExtension(abc));
        assertEquals("a.b"+File.separator+"c", StringUtil.removeExtension("a.b"+File.separator+"c"));
        assertEquals("a.b"+File.separator+"c", StringUtil.removeExtension("a.b"+File.separator+"c.txt"));
    }

    @Test
    public void pathProductTest() {
        assertEquals(Set.of("a","b","a/c","b/c"), StringUtil.pathProduct(Set.of("a", "b"), Set.of("c")));
    }

    @Test
    public void getArgumentTest() {
        String t = StringUtil.getArgument(new String[]{"param:abc"}, "param");
        assertEquals("abc", t);
    }

    @Test
    public void getArgumentNoParamTest() {
        String t = StringUtil.getArgument(new String[]{"help"}, "help");
        assertEquals("", t);
    }

    @Test
    public void getOverlapArg() {
        String t = StringUtil.getArgument(new String[]{"runtime-nodes:/runtime-nodes"}, "nodes");
        assertNull(t);
    }

}
