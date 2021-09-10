package scraper.test.plugins.core.flowgraph;

import scraper.api.DIContainer;
import scraper.api.ScrapeInstance;
import scraper.api.ScrapeSpecification;
import scraper.core.JobFactory;
import scraper.test.plugins.core.flowgraph.helper.InstanceHelper;
import scraper.util.DependencyInjectionUtil;
import scraper.utils.ClassUtil;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceUtil {

    public static ScrapeInstance read(String path) throws Exception {
        URL resource = ClassUtil.getResourceUrl(ResourceUtil.class, path);
        File scrapeFile = new File(resource.toURI());

        ScrapeSpecification spec = InstanceHelper.getInstance(scrapeFile);
        DIContainer dibean = DependencyInjectionUtil.getDIContainer();

        return dibean.get(JobFactory.class).convertScrapeJob(spec);
    }

    public static <T> T opt(Supplier<Optional<T>> o) {
        Optional<T> result = o.get();
        assertTrue(result.isPresent());
        return result.get();
    }

}

