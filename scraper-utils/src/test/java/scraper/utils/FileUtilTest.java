package scraper.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static scraper.utils.ClassUtil.sneakyThrow;

public class FileUtilTest {

    @Test
    public void watchTmpFileTest() throws IOException, InterruptedException {
        File tmp = File.createTempFile("pre","suff");
        tmp.deleteOnExit();
        AtomicBoolean notified = new AtomicBoolean(false);

        Thread t = new Thread(() ->{
            try {
                FileUtil.watchFile(tmp.getAbsolutePath(), filepath -> notified.set(true));
            } catch (IOException | InterruptedException e) {
                sneakyThrow(e);
            }
        });

        t.start();

        Thread.sleep(20);
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(tmp))) {
            pw.println("best content");
        }

        Thread.sleep(20);
        Assert.assertTrue(notified.get());
    }

    @Test(expected = IOException.class, timeout = 500)
    public void badWatchFileTest() throws IOException, InterruptedException {
        File tmp = new File("local");
        AtomicBoolean notified = new AtomicBoolean(false);
        FileUtil.watchFile(tmp.getPath(), filepath -> notified.set(true));
    }

    @Test
    public void globTest() throws IOException, InterruptedException {
        Path d = Files.createTempDirectory("suf");

        File tmp = new File(d.resolve("t1.txt").toString());
        tmp.deleteOnExit();
        File tmp2 = new File(d.resolve("ta.txt").toString());
        tmp2.deleteOnExit();
        File tmp4 = new File(d.resolve("ta.md").toString());
        tmp4.deleteOnExit();
        File tmpd = new File(d.resolve("abc").toString());
        File tmp3 = new File(d.resolve("abc/ta.md").toString());
        tmpd.deleteOnExit();
        tmp3.deleteOnExit();
        Assert.assertTrue(tmpd.mkdir());

        try (PrintWriter pw = new PrintWriter(new FileOutputStream(tmp))) {
            pw.println("best content");
        }
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(tmp2))) {
            pw.println("no");
        }
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(tmp3))) {
            pw.println("no");
        }
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(tmp4))) {
            pw.println("no");
        }

        Assert.assertTrue(tmpd.setReadable(false));

        Thread.sleep(100);

        File get = FileUtil.getFirstExisting("t1.txt", d.toString());
        Assert.assertNotNull(get);

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

    @Test
    public void replaceExtensionTest() {
        String t = FileUtil.replaceFileExtension("a.scrape","args");
        Assert.assertEquals("a.args", t);
    }

    @Test
    public void getFirstExistingInClassloaderTest() throws FileNotFoundException {
        File t = FileUtil.getFirstExisting("long.txt", "scraper/utils");
        Assert.assertTrue(t.exists());
    }

    @Test
    public void getParentPathTest() {
        Path parent = FileUtil.getParentPath("abc/test", "no");
        Assert.assertEquals("abc", parent.toString());
    }

    @Test
    public void watchFileFailTest() throws IOException, InterruptedException {
        File tmp = File.createTempFile("pre","suff");
        AtomicBoolean notified = new AtomicBoolean(false);
        AtomicBoolean error = new AtomicBoolean(false);

        Thread t = new Thread(() ->{
            try {
                FileUtil.watchFile(tmp.getAbsolutePath(), filepath -> notified.set(true));
                throw new RuntimeException("bad");
            } catch (IOException | InterruptedException e) {
                error.set(true);
            }
        });

        t.start();

        Thread.sleep(400);
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(tmp))) {
            pw.println("best content");
        }

        Thread.sleep(400);
        Assert.assertTrue(notified.get());
        Assert.assertTrue(tmp.delete());

        Thread.sleep(700);
        Assert.assertTrue(error.get());
    }
}
