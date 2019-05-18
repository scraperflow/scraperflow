package scraper.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class ClassUtilTest {
    @Test
    public void extractNodeCategoryTest() {
        Assert.assertEquals("testing", ClassUtil.extractCategoryOfNode("nodes.testing.testNode"));
    }

}
