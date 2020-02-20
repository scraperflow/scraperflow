package scraper.util;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.exceptions.ValidationException;
import scraper.api.specification.ScrapeSpecification;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;

public class JobUtilTest {

    @Test
    public void parseValidJsonSpecifications() throws IOException, ValidationException {
        URL folder = (getClass().getResource("job/specifications/valid/json"));
        File[] validFiles = new File(folder.getPath()).listFiles();
        Assert.assertNotNull(validFiles);
        for (File validFile : validFiles) {
            List<ScrapeSpecification> result = JobUtil.parseJobs(new String[]{validFile.getPath()}, Set.of());
            Assert.assertEquals("Bad file: "+validFile.getPath(), 1, result.size());
        }
    }

    @Test
    public void parseInvalidJsonSpecifications() {
        URL folder = (getClass().getResource("job/specifications/invalid/json"));
        File[] validFiles = new File(folder.getPath()).listFiles();
        Assert.assertNotNull(validFiles);
        for (File validFile : validFiles) {
            try {
                JobUtil.parseJobs(new String[]{validFile.getPath()}, Set.of());
                Assert.fail("Expected exception for file " + validFile.getPath());
            } catch (Exception ignored) {}
        }
    }

    @Test
    public void parseInvalidYmlSpecifications() {
        URL folder = (getClass().getResource("job/specifications/invalid/yml"));
        File[] validFiles = new File(folder.getPath()).listFiles();
        Assert.assertNotNull(validFiles);
        for (File validFile : validFiles) {
            try {
                JobUtil.parseJobs(new String[]{validFile.getPath()}, Set.of());
                throw new IllegalStateException("Expected exception for file " + validFile.getPath());
            } catch (Exception ignored) {}
        }
    }

    @Test
    public void parseValidYmlSpecifications() throws IOException, ValidationException {
        URL folder = (getClass().getResource("job/specifications/valid/yml"));
        File[] validFiles = new File(folder.getPath()).listFiles();
        Assert.assertNotNull(validFiles);
        for (File validFile : validFiles) {
            List<ScrapeSpecification> result = JobUtil.parseJobs(new String[]{validFile.getPath()}, Set.of());
            Assert.assertEquals("Bad file: "+validFile.getPath(), 1, result.size());
        }
    }

}