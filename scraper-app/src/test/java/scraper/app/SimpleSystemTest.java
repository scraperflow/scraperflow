package scraper.app;

import org.junit.Assert;
import org.junit.Test;
import scraper.utils.ClassUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.Permission;

public class SimpleSystemTest {

    // app should work even though banner can't be loaded
    @Test
    public void notExistingBannerTest() throws Exception {
        SecurityManager sm = System.getSecurityManager();
        try {
            System.setSecurityManager(new SecurityManager() {
                @Override public void checkPermission(Permission perm) {}
                @Override public void checkRead(String file) {
                    if(file.endsWith("banner.txt"))
                        for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
                            if ("scraper.app.Scraper".equals(elem.getClassName()) && "printBanner".equals(elem.getMethodName())) {
                                System.setProperty("thrown", "true");
                                ClassUtil.sneakyThrow(new IOException());
                            }
                        }
                }
            });
            Scraper.main(new String[0]);
        } finally {
            System.setSecurityManager(sm);
        }

        Assert.assertEquals("true", System.getProperty("thrown"));
    }

    @Test
    public void noopExitTest() throws Exception {
        Scraper.main(new String[0]);
        Assert.assertTrue(true);
    }

    @Test
    public void helpTest() throws Exception {
        Scraper.main(new String[]{"help"});
        Assert.assertTrue(true);
    }

    @Test
    public void exitTest() throws Exception {
        try {
            Scraper.main(new String[]{"exit"});
            Assert.assertTrue(true);
        } finally {
            System.setProperty("scraper.exit", "false");
        }
    }

    @Test
    public void pluginsLoadTest() throws Exception {
        Scraper.main(new String[]{});

        Assert.assertEquals("true", System.getProperty("noaddon"));
        Assert.assertEquals("true", System.getProperty("noplugin"));
        Assert.assertEquals("true", System.getProperty("noprehook"));
    }

    @Test
    public void loadJobTest() throws Exception {
        URL f = Scraper.class.getResource("t.jf");
        File ff = new File(f.toURI());
        Assert.assertTrue(ff.exists());
        Scraper.main(new String[]{ff.getAbsolutePath()});

        Thread.sleep(50);
        Assert.assertEquals("true", System.getProperty("done"));
    }

    @Test
    public void exceptionTest() throws Exception {
        URL f = Scraper.class.getResource("fail.jf");
        File ff = new File(f.toURI());
        Assert.assertTrue(ff.exists());
        Scraper.main(new String[]{ff.getAbsolutePath()});

    }

}
