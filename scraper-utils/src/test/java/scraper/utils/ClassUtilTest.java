package scraper.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class ClassUtilTest {
    @Test
    public void extractNodeCategoryTest() {
        Assert.assertEquals("testing", ClassUtil.extractCategoryOfNode("nodes.testing.testNode"));
    }

    @Test
    public void badNodeCategoryButNoExceptionTest() {
        ClassUtil.extractCategoryOfNode("abc");
        Assert.assertEquals("unknown", ClassUtil.extractCategoryOfNode("nodes."));
        Assert.assertEquals("unknown", ClassUtil.extractCategoryOfNode(".nodes"));
        Assert.assertEquals("", ClassUtil.extractCategoryOfNode(".nodes.."));
        Assert.assertEquals("unknown", ClassUtil.extractCategoryOfNode(".nodes."));
    }

}
