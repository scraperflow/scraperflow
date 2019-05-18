package scraper.app;

import org.junit.Assert;
import org.junit.Test;

public class SimpleSystemTest {
    @Test
    public void noopExitTest() {
        Scraper.main(new String[0]);
        Assert.assertTrue(true);
    }

    @Test
    public void helpTest() {
        Scraper.main(new String[]{"help"});
        Assert.assertTrue(true);
    }
}
