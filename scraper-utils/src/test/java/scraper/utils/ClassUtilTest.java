package scraper.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Test(expected = RuntimeException.class)
    public void sleepTest() {
        Thread m = Thread.currentThread();

        Thread t = new Thread(() -> {
            try {
                ClassUtil.sleep(1000);
            } catch (Exception e){
                m.interrupt();
            }

        });
        t.start();
        ClassUtil.sleep(1);
        t.interrupt();
        ClassUtil.sleep(100);
    }

    @Test
    public void sneakyTest() {
        try {
            ClassUtil.sneakyThrow(new IOException());
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IOException);
        }

    }
}
