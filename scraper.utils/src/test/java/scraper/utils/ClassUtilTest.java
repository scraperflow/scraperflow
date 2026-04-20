package scraper.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ClassUtilTest {
    @Test
    public void extractNodeCategoryTest() {
        assertEquals("testing", ClassUtil.extractCategoryOfNode("nodes.testing.testNode"));
    }

    @Test
    public void badNodeCategoryButNoExceptionTest() {
        ClassUtil.extractCategoryOfNode("abc");
        assertEquals("unknown", ClassUtil.extractCategoryOfNode("nodes."));
        assertEquals("unknown", ClassUtil.extractCategoryOfNode(".nodes"));
        assertEquals("", ClassUtil.extractCategoryOfNode(".nodes.."));
        assertEquals("unknown", ClassUtil.extractCategoryOfNode(".nodes."));
    }

    @Test
    public void sleepTest() {
        assertThrows(RuntimeException.class, () -> {
            Thread m = Thread.currentThread();

            Thread t = new Thread(() -> {
                try {
                    ClassUtil.sleep(1000);
                } catch (Exception e) {
                    m.interrupt();
                }

            });
            t.start();
            ClassUtil.sleep(1);
            t.interrupt();
            ClassUtil.sleep(100);
        });
    }

    @Test
    public void sneakyTest() {
        try {
            ClassUtil.sneakyThrow(new IOException());
        } catch (Exception e) {
            assertTrue(e instanceof IOException);
        }

    }
}
