package scraper.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scraper.api.exceptions.ValidationException;

import java.io.File;
import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleSystemTest {

//    @Rule
//    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @AfterEach
    public void setExit() {
        System.setProperty("scraper.exit", "false");
    }

    @Test
    public void noopExitTest() {
        assertThrows(Exception.class, () ->
                Scraper.main(new String[0])
        );
    }

    @Test
    public void helpTest() throws Exception {
        Scraper.main(new String[]{"help"});
        assertTrue(true);
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
        Scraper.main(new String[]{"exit"});

        assertEquals("true", System.getProperty("noaddon"));
        assertEquals("true", System.getProperty("noplugin"));
    }

    @SuppressWarnings("BusyWait") // under test
    @Test
    public void loadJobTest() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            URL f = Scraper.class.getResource("t.jf");
            File ff = new File(f.toURI());
            assertTrue(ff.exists());
            Scraper.main(new String[]{ff.getAbsolutePath()});

            while(!System.getProperty("done", "false").equalsIgnoreCase("true")) {
                Thread.sleep(10);
            }
        });
    }

    @Test
    public void exceptionTest() throws Exception {
        assertThrows(Exception.class, () -> {
            URL f = Scraper.class.getResource("fail.jf");
            File ff = new File(f.toURI());
            assertTrue(ff.exists());
            Scraper.main(new String[]{ff.getAbsolutePath()});
        });
    }

    @Test
    public void importTest() throws Exception {
        URL f = Scraper.class.getResource("complex.yf");
        File ff = new File(f.toURI());
        assertTrue(ff.exists());
        Scraper.main(new String[]{ff.getAbsolutePath()});
    }

    // do not allow access of nested instances
    @Test
    public void import2Test() throws Exception {
        assertThrows(ValidationException.class, () -> {
            URL f = Scraper.class.getResource("complex2.yf");
            File ff = new File(f.toURI());
            assertTrue(ff.exists());
            Scraper.main(new String[]{ff.getAbsolutePath()});
        });
    }

    // static type check with generics
    @Test
    public void genericsTestValid() throws Exception {
        URL f = Scraper.class.getResource("runtime.yf");
        File ff = new File(f.toURI());
        assertTrue(ff.exists());
        try {
            Scraper.main(new String[]{ff.getAbsolutePath()});
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void genericsTestInvalid() throws Exception {
        assertThrows(Exception.class, () -> {
            URL f = Scraper.class.getResource("runtime2.yf");
            File ff = new File(f.toURI());
            assertTrue(ff.exists());
            Scraper.main(new String[]{ff.getAbsolutePath()});
        });
    }

    @Test
    public void addrTest() throws Exception {
        URL f = Scraper.class.getResource("addr.yf");
        File ff = new File(f.toURI());
        assertTrue(ff.exists());
        Scraper.main(new String[]{ff.getAbsolutePath()});
    }
}
