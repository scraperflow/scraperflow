package scraper.app;

import org.junit.Assert;
import org.junit.BeforeClass;
//import org.junit.Rule;
import org.junit.Test;
//import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import scraper.api.exceptions.ValidationException;
import scraper.utils.ClassUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.Permission;

public class SimpleSystemTest {

//    @Rule
//    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @BeforeClass
    public static void enableExceptionExit() {
        System.setProperty("exitWithException", "true");
    }

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

    // does not work with gradle yet
//    @Test
//    public void exitTest() throws Exception {
//        exit.expectSystemExit();
//
//        try{
//            Scraper.main(new String[]{"exit"});
//        } finally {
//            System.setProperty("scraper.exit", "false");
//        }
//    }

    @Test
    public void pluginsLoadTest() throws Exception {
        Scraper.main(new String[]{});

        Assert.assertEquals("true", System.getProperty("noaddon"));
        Assert.assertEquals("true", System.getProperty("noplugin"));
    }

    @Test(timeout = 10000)
    public void loadJobTest() throws Exception {
        URL f = Scraper.class.getResource("t.jf");
        File ff = new File(f.toURI());
        Assert.assertTrue(ff.exists());
        Scraper.main(new String[]{ff.getAbsolutePath()});

        while(!System.getProperty("done", "false").equalsIgnoreCase("true")) {
            Thread.sleep(10);
        }
    }

    @Test(expected = Exception.class)
    public void exceptionTest() throws Exception {
        URL f = Scraper.class.getResource("fail.jf");
        File ff = new File(f.toURI());
        Assert.assertTrue(ff.exists());
        Scraper.main(new String[]{ff.getAbsolutePath()});

    }

    @Test
    public void importTest() throws Exception {
        URL f = Scraper.class.getResource("complex.yf");
        File ff = new File(f.toURI());
        Assert.assertTrue(ff.exists());
        Scraper.main(new String[]{ff.getAbsolutePath()});
    }

    // do not allow access of nested instances
    @Test(expected = ValidationException.class)
    public void import2Test() throws Exception {
        URL f = Scraper.class.getResource("complex2.yf");
        File ff = new File(f.toURI());
        Assert.assertTrue(ff.exists());
        Scraper.main(new String[]{ff.getAbsolutePath()});
    }

    // static type check with generics
    @Test
    public void genericsTestValid() throws Exception {
        URL f = Scraper.class.getResource("runtime.yf");
        File ff = new File(f.toURI());
        Assert.assertTrue(ff.exists());
        try {
            Scraper.main(new String[]{ff.getAbsolutePath()});
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test(expected = Exception.class)
    public void genericsTestInvalid() throws Exception {
        URL f = Scraper.class.getResource("runtime2.yf");
        File ff = new File(f.toURI());
        Assert.assertTrue(ff.exists());
        Scraper.main(new String[]{ff.getAbsolutePath()});
    }

    @Test
    public void addrTest() throws Exception {
        URL f = Scraper.class.getResource("addr.yf");
        File ff = new File(f.toURI());
        Assert.assertTrue(ff.exists());
        Scraper.main(new String[]{ff.getAbsolutePath()});
    }
}
