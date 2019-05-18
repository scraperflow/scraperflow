package scraper.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FileUtilTest {

    @Test
    public void watchTmpFile() throws IOException, InterruptedException {
        File tmp = File.createTempFile("pre","suff");
        tmp.deleteOnExit();
        AtomicBoolean notified = new AtomicBoolean(false);

        Thread t = new Thread(() ->{
            try {
                FileUtil.watchFile(tmp.getAbsolutePath(), filepath -> {
                    notified.set(true);
                });
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        t.start();

        Thread.sleep(100);
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(tmp))) {
            pw.println("best content");
        }

        Thread.sleep(100);
        Assert.assertTrue(notified.get());
    }

    @Test
    public void globTest() throws IOException, InterruptedException {
        Path d = Files.createTempDirectory("suf");

        File tmp = new File(d.resolve("t1.txt").toString());
        tmp.deleteOnExit();
        File tmp2 = new File(d.resolve("ta.txt").toString());
        tmp2.deleteOnExit();
        File tmp3 = new File(d.resolve("ta.md").toString());
        tmp3.deleteOnExit();


        try (PrintWriter pw = new PrintWriter(new FileOutputStream(tmp))) {
            pw.println("best content");
        }
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(tmp2))) {
            pw.println("no");
        }

        Thread.sleep(100);

        FileUtil.getFirstExisting("t1.txt", d.toString());

        AtomicInteger matched = new AtomicInteger();
        FileUtil.match("glob:**/*.txt", d.toString(), path -> matched.getAndIncrement());

        Assert.assertEquals(2, matched.get());

    }

    @Test
    public void readFile() throws IOException {
        URL url = getClass().getResource("long.txt");
        Assert.assertNotNull(url);

        File f = new File(url.getPath());
        Assert.assertTrue(f.exists());

        Assert.assertEquals(5, StringUtil.readBodyToList(f).size());
        Assert.assertEquals("this\nis a log\n\\;\n\nmultiple lines\n", StringUtil.readBody(f));
    }

}
