//package scraper.services.impl;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import scraper.api.service.FileService;
//import scraper.api.service.impl.FileServiceImpl;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Path;
//
//@SuppressWarnings("ResultOfMethodCallIgnored")
//public class FileServiceImplTest {
//
//    File root;
//    Path rootP;
//    FileService f;
//
//    @Before
//    public void initDir() {
//        root = Files.createTempDir();
//        root.deleteOnExit();
//        rootP = root.toPath();
//
//        f = new FileServiceImpl();
//    }
//
//
//    @Test
//    public void simpleTest() throws IOException {
//        Assert.assertTrue(f.getTemporaryDirectory().exists());
//
//        File toRead = rootP.resolve("test.txt").toFile();
//        toRead.delete();
//        toRead.deleteOnExit();
//        String p = toRead.getAbsolutePath();
//
//        f.ensureFile(p);
//        f.appendToFile(p, "hello");
//        f.ensureDirectory(toRead);
//
//        Assert.assertTrue(f.containsLineStartsWith(p, "hel"));
//        Assert.assertFalse(f.containsLineStartsWith(p, "ello"));
//
//        f.replaceFile(p, "ok");
//        Assert.assertFalse(f.containsLineStartsWith(p, "hello"));
//        Assert.assertTrue(f.containsLineStartsWith(p, "ok"));
//
//        // ensure dir
//        File dirs = rootP.resolve("a/c").toFile();
//        dirs.getParentFile().deleteOnExit();
//        dirs.deleteOnExit();
//        f.ensureDirectory(dirs);
//        f.ensureFile(dirs.getAbsolutePath());
//        Assert.assertTrue(dirs.exists() && dirs.isFile());
//    }
//}
